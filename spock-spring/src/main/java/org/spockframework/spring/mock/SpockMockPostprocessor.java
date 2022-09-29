/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring.mock;

import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.spring.SpringBean;
import org.spockframework.spring.SpringExtensionException;
import org.spockframework.spring.SpringSpy;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

/**
 * A {@link BeanFactoryPostProcessor} used to register and inject
 * {@link SpringBean @SpringBean} and wrap spies using {@link SpringSpy @SpringSpy} with the {@link ApplicationContext}.
 *
 * original authors Phillip Webb, Andy Wilkinson Stephane Nicoll
 *
 * @author Leonard Brünings
 * @since 1.2
 */

public class SpockMockPostprocessor extends BackwardsCompatibleInstantiationAwareBeanPostProcessorAdapter
  implements BeanFactoryPostProcessor, Ordered {

  private static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

  private static final String BEAN_NAME = SpockMockPostprocessor.class.getName();

  private final Set<Definition> definitions;

  private final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

  private Map<String, SpyDefinition> spies = new HashMap<>();

  private BeanFactory beanFactory;


  /**
   * Create a new {@link SpockMockPostprocessor} instance with the given initial
   * definitions.
   *
   * @param definitions the initial definitions
   */
  public SpockMockPostprocessor(Set<Definition> definitions) {
    this.definitions = definitions;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory,
      "SpockMockPostprocessor can only be used on bean factories that "
        + "implement BeanDefinitionRegistry");
    postProcessBeanFactory(beanFactory, (BeanDefinitionRegistry)beanFactory);
  }

  private void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory,
                                      BeanDefinitionRegistry registry) {
    this.beanFactory = beanFactory;
    for (Definition definition : definitions) {
      register(beanFactory, registry, definition);
    }
  }

  private void register(ConfigurableListableBeanFactory beanFactory,
                        BeanDefinitionRegistry registry, Definition definition) {
    if (definition instanceof MockDefinition) {
      registerMock(beanFactory, registry, (MockDefinition)definition);
    } else if (definition instanceof SpyDefinition) {
      registerSpy(beanFactory, registry, (SpyDefinition)definition);
    }
  }

  private void registerMock(ConfigurableListableBeanFactory beanFactory,
                            BeanDefinitionRegistry registry, MockDefinition definition) {
    BeanDefinition beanDefinition = createBeanDefinition(definition);
    String beanName = getBeanName(beanFactory, registry, definition, beanDefinition);
    String transformedBeanName = BeanFactoryUtils.transformedBeanName(beanName);
    beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, beanName);
    if (registry.containsBeanDefinition(transformedBeanName)) {
      BeanDefinition existing = registry.getBeanDefinition(transformedBeanName);
      copyBeanDefinitionDetails(existing, beanDefinition);
      registry.removeBeanDefinition(transformedBeanName);
    }
    registry.registerBeanDefinition(transformedBeanName, beanDefinition);
    Object mock = createMock(definition, beanName);
    beanFactory.registerSingleton(transformedBeanName, mock);
    registerAliases(beanFactory, definition, transformedBeanName);
  }

  private void registerAliases(ConfigurableListableBeanFactory beanFactory,
                               MockDefinition definition, String beanName) {
    for (String alias : definition.getAliases()) {
      beanFactory.registerAlias(beanName, alias);
    }
  }

  private BeanDefinition createBeanDefinition(MockDefinition mockDefinition) {
    RootBeanDefinition definition = new RootBeanDefinition(mockDefinition.getTypeToMock().resolve());
    definition.setTargetType(mockDefinition.getTypeToMock());
    definition.setFactoryBeanName(BEAN_NAME);
    definition.setFactoryMethodName("createMock");
    definition.getConstructorArgumentValues().addIndexedArgumentValue(0, mockDefinition);
    if (mockDefinition.getQualifier() != null) {
      mockDefinition.getQualifier().applyTo(definition);
    }
    return definition;
  }

  /**
   * Factory method used by defined beans to actually create the mock.
   *
   * @param mockDefinition the mock definition
   * @param name the bean name
   * @return the mock instance
   */
  private Object createMock(MockDefinition mockDefinition, String name) {
    return mockDefinition.createMock(name + " bean");
  }

  private String getBeanName(ConfigurableListableBeanFactory beanFactory,
                             BeanDefinitionRegistry registry, MockDefinition mockDefinition,
                             BeanDefinition beanDefinition) {
    if (StringUtils.hasLength(mockDefinition.getName())) {
      return mockDefinition.getName();
    }
    Set<String> existingBeans = getExistingBeans(beanFactory, mockDefinition);
    if (existingBeans.isEmpty()) {
      return this.beanNameGenerator.generateBeanName(beanDefinition, registry);
    }
    if (existingBeans.size() == 1) {
      return existingBeans.iterator().next();
    }
    String primaryCandidate = determinePrimaryCandidate(registry, existingBeans, mockDefinition.getTypeToMock());
    if (primaryCandidate != null) {
      return primaryCandidate;
    }
    throw new IllegalStateException(
      "Unable to register mock bean " + mockDefinition.getTypeToMock()
        + " expected a single matching bean to replace but found "
        + existingBeans);
  }

  private void copyBeanDefinitionDetails(BeanDefinition from, BeanDefinition to) {
    to.setPrimary(from.isPrimary());
  }

  private void registerSpy(ConfigurableListableBeanFactory beanFactory,
                           BeanDefinitionRegistry registry, SpyDefinition definition) {
    Set<String> existingBeans = getExistingBeans(beanFactory, definition.getTypeToSpy());
    if (ObjectUtils.isEmpty(existingBeans)) {
      FieldInfo fieldInfo = definition.getFieldInfo();
      throw new SpringExtensionException(String.format("No matching bean found! " +
        "@SpringSpy requires an existing spring bean to wrap, to create a standalone spy use @SpringBean.%n" +
        "Offending Field: '%s.%s:%d'", fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine()));
    } else {
      registerSpies(registry, definition, existingBeans);
    }
  }

  private Set<String> getExistingBeans(ConfigurableListableBeanFactory beanFactory,
                                       MockDefinition mockDefinition) {
    QualifierDefinition qualifier = mockDefinition.getQualifier();
    Set<String> candidates = new TreeSet<>();
    for (String candidate : getExistingBeans(beanFactory,
      mockDefinition.getTypeToMock())) {
      if (qualifier == null || qualifier.matches(beanFactory, candidate)) {
        candidates.add(candidate);
      }
    }
    return candidates;
  }

  private Set<String> getExistingBeans(ConfigurableListableBeanFactory beanFactory,
                                    ResolvableType type) {
    Set<String> beans = new LinkedHashSet<>(
      asList(beanFactory.getBeanNamesForType(type)));
    String resolvedTypeName = type.resolve(Object.class).getName();
    for (String beanName : beanFactory.getBeanNamesForType(FactoryBean.class)) {
      beanName = BeanFactoryUtils.transformedBeanName(beanName);
      BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      if (resolvedTypeName.equals(beanDefinition.getAttribute(FACTORY_BEAN_OBJECT_TYPE))) {
        beans.add(beanName);
      }
    }

    beans.removeIf(this::isScopedTarget);
    return beans;
  }

  private boolean isScopedTarget(String beanName) {
    try {
      return ScopedProxyUtils.isScopedTarget(beanName);
    } catch (Throwable ex) {
      return false;
    }
  }

  private void registerSpies(BeanDefinitionRegistry registry, SpyDefinition definition,
                             Set<String> existingBeans) {
    try {
      registerSpy(definition,
        determineBeanName(existingBeans, definition, registry));
    } catch (RuntimeException ex) {
      throw new IllegalStateException(
        "Unable to register spy bean " + definition.getTypeToSpy(), ex);
    }
  }

  private String determineBeanName(Collection<String> existingBeans, SpyDefinition definition,
                                   BeanDefinitionRegistry registry) {
    if (StringUtils.hasText(definition.getName())) {
      return definition.getName();
    }
    if (existingBeans.size() == 1) {
      return existingBeans.iterator().next();
    }
    return determinePrimaryCandidate(registry, existingBeans, definition.getTypeToSpy());
  }

  private String determinePrimaryCandidate(BeanDefinitionRegistry registry,
                                           Collection<String> candidateBeanNames, ResolvableType type) {
    String primaryBeanName = null;
    for (String candidateBeanName : candidateBeanNames) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(candidateBeanName);
      if (beanDefinition.isPrimary()) {
        if (primaryBeanName != null) {
          throw new NoUniqueBeanDefinitionException(type.resolve(),
            candidateBeanNames.size(),
            "more than one 'primary' bean found among candidates: "
              + candidateBeanNames);
        }
        primaryBeanName = candidateBeanName;
      }
    }
    return primaryBeanName;
  }

  private void registerSpy(SpyDefinition definition, String beanName) {
    this.spies.put(beanName, definition);
  }

  Object createSpyIfNecessary(Object bean, String beanName)
    throws BeansException {
    SpyDefinition definition = this.spies.get(beanName);
    if (definition != null) {
      bean = definition.createSpy(beanName, bean);
    }
    return bean;
  }


  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 10;
  }

  /**
   * Register the processor with a {@link BeanDefinitionRegistry}. Not required when
   * using the Spocks {@link spock.lang.Specification} as registration is automatic.
   *
   * @param registry the bean definition registry
   * @param definitions the initial mock/spy definitions
   */
  static void register(BeanDefinitionRegistry registry,
                       Set<Definition> definitions) {
    register(registry, SpockMockPostprocessor.class, definitions);
  }

  /**
   * Register the processor with a {@link BeanDefinitionRegistry}. Not required when
   * using the Spocks {@link spock.lang.Specification} as registration is automatic.
   *
   * @param registry the bean definition registry
   * @param postProcessor the post processor class to register
   * @param definitions the initial mock/spy definitions
   */
  @SuppressWarnings("unchecked")
  static void register(BeanDefinitionRegistry registry,
                       Class<? extends SpockMockPostprocessor> postProcessor,
                       Set<Definition> definitions) {
    SpyPostProcessor.register(registry);
    BeanDefinition definition = getOrAddBeanDefinition(registry, postProcessor);
    ValueHolder constructorArg = definition.getConstructorArgumentValues().getIndexedArgumentValue(0, Set.class);
    Set<Definition> existing = (Set<Definition>)constructorArg.getValue();
    if (definitions != null) {
      existing.addAll(definitions);
    }
  }

  private static BeanDefinition getOrAddBeanDefinition(BeanDefinitionRegistry registry,
                                                       Class<? extends SpockMockPostprocessor> postProcessor) {
    if (!registry.containsBeanDefinition(BEAN_NAME)) {
      RootBeanDefinition definition = new RootBeanDefinition(postProcessor);
      definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
      constructorArguments.addIndexedArgumentValue(0, new LinkedHashSet<SpockDefinition>());
      registry.registerBeanDefinition(BEAN_NAME, definition);
      return definition;
    }
    return registry.getBeanDefinition(BEAN_NAME);
  }

  public void injectSpies(Object testInstance) {
    for (Map.Entry<String, SpyDefinition> spyDefinitionEntry : spies.entrySet()) {
      FieldInfo fieldInfo = spyDefinitionEntry.getValue().getFieldInfo();
      Object spy = beanFactory.getBean(spyDefinitionEntry.getKey(), fieldInfo.getType());
      fieldInfo.writeValue(testInstance, spy);
    }
  }

  /**
   * {@link BeanPostProcessor} to handle {@link SpringSpy} definitions. Registered as a
   * separate processor so that it can be ordered above AOP post processors.
   */
  static class SpyPostProcessor extends BackwardsCompatibleInstantiationAwareBeanPostProcessorAdapter
    implements PriorityOrdered {

    private static final String BEAN_NAME = SpyPostProcessor.class.getName();

    private final Map<String, Object> earlySpyReferences = new ConcurrentHashMap<>(16);

    private final SpockMockPostprocessor spockMockPostprocessor;

    SpyPostProcessor(SpockMockPostprocessor spockMockPostprocessor) {
      this.spockMockPostprocessor = spockMockPostprocessor;
    }

    @Override
    public int getOrder() {
      return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName)
      throws BeansException {
      if (bean instanceof FactoryBean) {
        return bean;
      }
      this.earlySpyReferences.put(getCacheKey(bean, beanName), bean);
      return createSpyIfNecessary(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
      if (bean instanceof FactoryBean) {
        return bean;
      }
      if (this.earlySpyReferences.remove(getCacheKey(bean, beanName)) != bean) {
        return this.spockMockPostprocessor.createSpyIfNecessary(bean, beanName);
      }
      return bean;
    }

    private Object createSpyIfNecessary(Object bean, String beanName) {
      return this.spockMockPostprocessor.createSpyIfNecessary(bean, beanName);
    }
    private String getCacheKey(Object bean, String beanName) {
      return StringUtils.hasLength(beanName) ? beanName : bean.getClass().getName();
    }

    static void register(BeanDefinitionRegistry registry) {
      if (!registry.containsBeanDefinition(BEAN_NAME)) {
        RootBeanDefinition definition = new RootBeanDefinition(SpyPostProcessor.class);
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
        constructorArguments.addIndexedArgumentValue(0, new RuntimeBeanReference(SpockMockPostprocessor.BEAN_NAME));
        registry.registerBeanDefinition(BEAN_NAME, definition);
      }
    }

  }
}
