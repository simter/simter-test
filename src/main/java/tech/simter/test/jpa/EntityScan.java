package tech.simter.test.jpa;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * The annotation for just config concrete entities to JPA.
 * <p>
 * Not like {@link org.springframework.boot.autoconfigure.domain.EntityScan},
 * it adds all entities below the config package.
 *
 * @author RJ
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EntityScanClasses.Registrar.class)
public @interface EntityScan {
  /**
   * Add managed class to the persistence provider's metadata.
   *
   * @see javax.persistence.spi.PersistenceUnitInfo#getManagedClassNames()
   * @return managed classes
   */
  Class<?>[] value() default {};
}
