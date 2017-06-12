/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.simter.test.jpa;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Class for storing {@link tech.simter.test.jpa.EntityScan} specified entity classes for reference later
 * (e.g. by JPA auto-configuration).
 *
 * @author RJ
 * @see tech.simter.test.jpa.EntityScan
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 * @see org.springframework.boot.autoconfigure.domain.EntityScanPackages
 * @see JpaBaseConfiguration#entityManagerFactory(org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder)
 */
public class EntityScanClasses {
  private static final String BEAN = EntityScanClasses.class.getName();
  private static final EntityScanClasses NONE = new EntityScanClasses();
  private final List<Class<?>> entityClasses;

  EntityScanClasses(Class<?>... entityClasses) {
    List<Class<?>> classes = new ArrayList<>();
    Collections.addAll(classes, entityClasses);
    this.entityClasses = Collections.unmodifiableList(classes);
  }

  /**
   * Return the entity classes specified from all {@link tech.simter.test.jpa.EntityScan} annotations.
   *
   * @return the entity classes
   */
  public List<Class<?>> getEntityClasses() {
    return this.entityClasses;
  }

  /**
   * Return the {@link EntityScanClasses} for the given bean factory.
   *
   * @param beanFactory the source bean factory
   * @return the {@link EntityScanClasses} for the bean factory (never {@code null})
   */
  public static EntityScanClasses get(BeanFactory beanFactory) {
    try {
      return beanFactory.getBean(BEAN, EntityScanClasses.class);
    } catch (NoSuchBeanDefinitionException ex) {
      return NONE;
    }
  }

  /**
   * Register the specified entity classes with the system.
   *
   * @param registry      the source registry
   * @param entityClasses the entity classes to register
   */
  public static void register(BeanDefinitionRegistry registry, Class<?>... entityClasses) {
    Assert.notNull(registry, "Registry must not be null");
    Assert.notNull(entityClasses, "EntityClasses must not be null");
    register(registry, Arrays.asList(entityClasses));
  }

  /**
   * Register the specified entity classes with the system.
   *
   * @param registry      the source registry
   * @param entityClasses the entity classes to register
   */
  public static void register(BeanDefinitionRegistry registry, Collection<Class<?>> entityClasses) {
    Assert.notNull(registry, "Registry must not be null");
    Assert.notNull(entityClasses, "EntityClasses must not be null");
    if (registry.containsBeanDefinition(BEAN)) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);
      ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();
      constructorArguments.addIndexedArgumentValue(0, addEntityClasses(constructorArguments, entityClasses));
    } else {
      GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
      beanDefinition.setBeanClass(EntityScanClasses.class);
      beanDefinition.getConstructorArgumentValues()
        .addIndexedArgumentValue(0, entityClasses.toArray(new Class<?>[entityClasses.size()]));
      beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      registry.registerBeanDefinition(BEAN, beanDefinition);
    }
  }

  private static Class<?>[] addEntityClasses(ConstructorArgumentValues constructorArguments,
                                             Collection<Class<?>> entityClasses) {
    Class<?>[] existing = (Class<?>[]) constructorArguments.getIndexedArgumentValue(0, String[].class).getValue();
    Set<Class<?>> merged = new LinkedHashSet<>();
    merged.addAll(Arrays.asList(existing));
    merged.addAll(entityClasses);
    return merged.toArray(new Class<?>[merged.size()]);
  }

  /**
   * {@link ImportBeanDefinitionRegistrar} to store the entity classes from the importing configuration.
   */
  @Order(Ordered.HIGHEST_PRECEDENCE)
  static class Registrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {
      register(registry, getEntityClasses(metadata));
    }

    private Set<Class<?>> getEntityClasses(AnnotationMetadata metadata) {
      AnnotationAttributes attributes = AnnotationAttributes.fromMap(
        metadata.getAnnotationAttributes(EntityScan.class.getName())
      );
      Class<?>[] classes = attributes.getClassArray("value");
      Set<Class<?>> result = new LinkedHashSet<>();
      Collections.addAll(result, classes);
      return result;
    }
  }
}