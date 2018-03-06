package tech.simter.test.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;

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
@TestConfiguration
public class JpaTestConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(JpaTestConfiguration.class);
  private BeanFactory beanFactory;

  public JpaTestConfiguration(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Bean("persistenceUnitManager")
  @Primary
  public PersistenceUnitManager persistenceUnitManager() {
    return new SimterPersistenceUnitManager(EntityScanClasses.get(this.beanFactory).getEntityClasses());
  }
}