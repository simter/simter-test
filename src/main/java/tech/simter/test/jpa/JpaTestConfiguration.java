package tech.simter.test.jpa;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Extend {@link HibernateJpaAutoConfiguration} to make it support config concrete entity classes.
 * <p>
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
 *
 * @author RJ
 */
@TestConfiguration
public class JpaTestConfiguration extends HibernateJpaAutoConfiguration {
  private BeanFactory beanFactory;

  public JpaTestConfiguration(DataSource dataSource, JpaProperties jpaProperties,
                              ObjectProvider<JtaTransactionManager> jtaTransactionManager,
                              ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
    super(dataSource, jpaProperties, jtaTransactionManager, transactionManagerCustomizers);
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    super.setBeanFactory(beanFactory);
    this.beanFactory = beanFactory;
  }

  @Bean
  @Primary
  @ConditionalOnMissingBean({LocalContainerEntityManagerFactoryBean.class, EntityManagerFactory.class})
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder) {
    LocalContainerEntityManagerFactoryBean bean = super.entityManagerFactory(factoryBuilder);
    bean.setPersistenceUnitPostProcessors(persistenceUnit ->
      EntityScanClasses.get(this.beanFactory).getEntityClasses().forEach(entityClass ->
        persistenceUnit.addManagedClassName(entityClass.getName())
      )
    );
    return bean;
  }
}