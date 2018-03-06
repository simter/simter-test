package tech.simter.test.jpa;

import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.SchemaManagementProvider;
import org.springframework.boot.jdbc.metadata.CompositeDataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringJtaPlatform;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.*;

/**
 * Extend {@link HibernateJpaAutoConfiguration} to make it support config concrete entity classes.
 * <pre>
 *   Example:
 *
 *   import tech.simter.test.jpa.JpaTestConfiguration;
 *
 *   &#064;RunWith(SpringRunner.class)
 *   &#064;ContextConfiguration(classes = {MyDaoJpaImpl.class, JpaTestConfiguration.class})
 *   &#064;DataJpaTest
 *   &#064;tech.simter.test.jpa.EntityScan(MyPO.class)
 *   public class MyDaoJpaImplTest {
 *     ...
 *   }
 * </pre>
 * <p>
 * See <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide#jpa-and-spring-data">Spring-Boot-2.0-Migration-Guide For JPA-and-Spring-Data</a>.
 *
 * @author RJ
 */
//@TestConfiguration
public class JpaTestConfigurationTemp {
  private static final Logger logger = LoggerFactory.getLogger(JpaTestConfiguration.class);
  private final HibernateDefaultDdlAutoProvider defaultDdlAutoProvider;
  private final PhysicalNamingStrategy physicalNamingStrategy;
  private final ImplicitNamingStrategy implicitNamingStrategy;
  private final List<HibernatePropertiesCustomizer> hibernatePropertiesCustomizers;
  private final JpaProperties properties;
  private final JtaTransactionManager jtaTransactionManager;
  private final DataSourcePoolMetadataProvider poolMetadataProvider;
  private BeanFactory beanFactory;
  private DataSource dataSource;

  public JpaTestConfigurationTemp(BeanFactory beanFactory,
                              DataSource dataSource,
                              JpaProperties jpaProperties,
                              ObjectProvider<JtaTransactionManager> jtaTransactionManager,
                              ObjectProvider<Collection<DataSourcePoolMetadataProvider>> metadataProviders,
                              ObjectProvider<List<SchemaManagementProvider>> providers,
                              ObjectProvider<PhysicalNamingStrategy> physicalNamingStrategy,
                              ObjectProvider<ImplicitNamingStrategy> implicitNamingStrategy,
                              ObjectProvider<List<HibernatePropertiesCustomizer>> hibernatePropertiesCustomizers) {
    this.beanFactory = beanFactory;
    this.dataSource = dataSource;
    this.properties = jpaProperties;
    this.jtaTransactionManager = jtaTransactionManager.getIfAvailable();
    this.defaultDdlAutoProvider = new HibernateDefaultDdlAutoProvider(
      providers.getIfAvailable(Collections::emptyList));
    this.poolMetadataProvider = new CompositeDataSourcePoolMetadataProvider(
      metadataProviders.getIfAvailable());
    this.physicalNamingStrategy = physicalNamingStrategy.getIfAvailable();
    this.implicitNamingStrategy = implicitNamingStrategy.getIfAvailable();
    this.hibernatePropertiesCustomizers = hibernatePropertiesCustomizers
      .getIfAvailable(Collections::emptyList);
  }

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
    EntityManagerFactoryBuilder factoryBuilder,
    DataSource dataSource) {
    Map<String, Object> vendorProperties = getVendorProperties();
    customizeVendorProperties(vendorProperties);
    LocalContainerEntityManagerFactoryBean bean = factoryBuilder.dataSource(dataSource)
      .properties(vendorProperties)
      .jta(isJta()).build();

    bean.setPersistenceUnitPostProcessors(persistenceUnit ->
      EntityScanClasses.get(this.beanFactory).getEntityClasses().forEach(entityClass ->
        persistenceUnit.addManagedClassName(entityClass.getName())
      )
    );
    return bean;
  }

  private Map<String, Object> getVendorProperties() {
    String defaultDdlMode = defaultDdlAutoProvider.getDefaultDdlAuto(this.dataSource);
    return new LinkedHashMap<>(
      this.properties.getHibernateProperties(
        new HibernateSettings().ddlAuto(defaultDdlMode)
          .implicitNamingStrategy(this.implicitNamingStrategy)
          .physicalNamingStrategy(this.physicalNamingStrategy)
          .hibernatePropertiesCustomizers(this.hibernatePropertiesCustomizers)
      )
    );
  }

  private static final String JTA_PLATFORM = "hibernate.transaction.jta.platform";
  private static final String PROVIDER_DISABLES_AUTOCOMMIT = "hibernate.connection.provider_disables_autocommit";

  private void customizeVendorProperties(Map<String, Object> vendorProperties) {
    if (!vendorProperties.containsKey(JTA_PLATFORM)) {
      configureJtaPlatform(vendorProperties);
    }
    if (!vendorProperties.containsKey(PROVIDER_DISABLES_AUTOCOMMIT)) {
      configureProviderDisablesAutocommit(vendorProperties);
    }
  }

  private void configureJtaPlatform(Map<String, Object> vendorProperties)
    throws LinkageError {
    JtaTransactionManager jtaTransactionManager = this.jtaTransactionManager;
    if (jtaTransactionManager != null) {
      if (runningOnWebSphere()) {
        // We can never use SpringJtaPlatform on WebSphere as
        // WebSphereUowTransactionManager has a null TransactionManager
        // which will cause Hibernate to NPE
        configureWebSphereTransactionPlatform(vendorProperties);
      } else {
        configureSpringJtaPlatform(vendorProperties, jtaTransactionManager);
      }
    } else {
      vendorProperties.put(JTA_PLATFORM, getNoJtaPlatformManager());
    }
  }

  private void configureProviderDisablesAutocommit(
    Map<String, Object> vendorProperties) {
    if (isDataSourceAutoCommitDisabled() && !isJta()) {
      vendorProperties.put(PROVIDER_DISABLES_AUTOCOMMIT, "true");
    }
  }

  private boolean isDataSourceAutoCommitDisabled() {
    DataSourcePoolMetadata poolMetadata = this.poolMetadataProvider
      .getDataSourcePoolMetadata(this.dataSource);
    return poolMetadata != null
      && Boolean.FALSE.equals(poolMetadata.getDefaultAutoCommit());
  }

  private boolean runningOnWebSphere() {
    return ClassUtils.isPresent(
      "com.ibm.websphere.jtaextensions.ExtendedJTATransaction",
      getClass().getClassLoader());
  }

  private void configureWebSphereTransactionPlatform(
    Map<String, Object> vendorProperties) {
    vendorProperties.put(JTA_PLATFORM, getWebSphereJtaPlatformManager());
  }

  private static final String[] WEBSPHERE_JTA_PLATFORM_CLASSES = {
    "org.hibernate.engine.transaction.jta.platform.internal.WebSphereExtendedJtaPlatform",
    "org.hibernate.service.jta.platform.internal.WebSphereExtendedJtaPlatform"};

  private Object getWebSphereJtaPlatformManager() {
    return getJtaPlatformManager(WEBSPHERE_JTA_PLATFORM_CLASSES);
  }

  private Object getJtaPlatformManager(String[] candidates) {
    for (String candidate : candidates) {
      try {
        return Class.forName(candidate).newInstance();
      } catch (Exception ex) {
        // Continue searching
      }
    }
    throw new IllegalStateException("Could not configure JTA platform");
  }

  private void configureSpringJtaPlatform(Map<String, Object> vendorProperties,
                                          JtaTransactionManager jtaTransactionManager) {
    try {
      vendorProperties.put(JTA_PLATFORM,
        new SpringJtaPlatform(jtaTransactionManager));
    } catch (LinkageError ex) {
      // NoClassDefFoundError can happen if Hibernate 4.2 is used and some
      // containers (e.g. JBoss EAP 6) wrap it in the superclass LinkageError
      if (!isUsingJndi()) {
        throw new IllegalStateException("Unable to set Hibernate JTA "
          + "platform, are you using the correct "
          + "version of Hibernate?", ex);
      }
      // Assume that Hibernate will use JNDI
      if (logger.isDebugEnabled()) {
        logger.debug("Unable to set Hibernate JTA platform : " + ex.getMessage());
      }
    }
  }

  private boolean isUsingJndi() {
    try {
      return JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable();
    } catch (Error ex) {
      return false;
    }
  }

  private static final String[] NO_JTA_PLATFORM_CLASSES = {
    "org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform",
    "org.hibernate.service.jta.platform.internal.NoJtaPlatform"};

  private Object getNoJtaPlatformManager() {
    return getJtaPlatformManager(NO_JTA_PLATFORM_CLASSES);
  }

  private boolean isJta() {
    return (this.jtaTransactionManager != null);
  }
}