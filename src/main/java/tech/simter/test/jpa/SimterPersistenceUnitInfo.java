package tech.simter.test.jpa;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public class SimterPersistenceUnitInfo extends MutablePersistenceUnitInfo {
  @Override
  public ClassLoader getNewTempClassLoader() {
    return null;
  }
}
