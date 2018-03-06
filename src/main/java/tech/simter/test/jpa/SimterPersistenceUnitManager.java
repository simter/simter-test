package tech.simter.test.jpa;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;

import javax.persistence.spi.PersistenceUnitInfo;
import java.util.List;

//@Component
public class SimterPersistenceUnitManager implements PersistenceUnitManager, InitializingBean {
  private final List<Class<?>> entityClasses;
  private PersistenceUnitInfo persistenceUnitInfo;

  public SimterPersistenceUnitManager(List<Class<?>> entityClasses) {
    this.entityClasses = entityClasses;
  }

  @Override
  public void afterPropertiesSet() {
    this.persistenceUnitInfo = buildDefaultPersistenceUnitInfo();
  }

  private PersistenceUnitInfo buildDefaultPersistenceUnitInfo() {
    SimterPersistenceUnitInfo persistenceUnit = new SimterPersistenceUnitInfo();
    persistenceUnit.setPersistenceUnitName(DefaultPersistenceUnitManager.ORIGINAL_DEFAULT_PERSISTENCE_UNIT_NAME);
    persistenceUnit.setExcludeUnlistedClasses(true);

    if (entityClasses != null)
      entityClasses.forEach(entityClass ->
        persistenceUnit.addManagedClassName(entityClass.getName())
      );

    return persistenceUnit;
  }

  @Override
  public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() throws IllegalStateException {
    return persistenceUnitInfo;
  }

  @Override
  public PersistenceUnitInfo obtainPersistenceUnitInfo(String persistenceUnitName) throws IllegalArgumentException, IllegalStateException {
    return persistenceUnitInfo;
  }
}
