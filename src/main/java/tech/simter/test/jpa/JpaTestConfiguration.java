package tech.simter.test.jpa;

import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * The {@link HibernateJpaAutoConfiguration} can not be extended since spring-boot-2.0.0.RELEASE.
 * <p>
 * It's really upset.
 *
 * @author RJ
 */
@TestConfiguration
@Deprecated
public class JpaTestConfiguration extends HibernateJpaAutoConfiguration {
}