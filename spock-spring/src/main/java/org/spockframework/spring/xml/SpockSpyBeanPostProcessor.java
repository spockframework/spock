/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring.xml;

import org.spockframework.mock.MockNature;
import spock.mock.DetachedMockFactory;

import java.util.Collections;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;

/**
 * Wraps a given Spring bean with a detached Spock spy
 *
 * Spring integration of spock mocks is heavily inspired by
 * Springockito {@see https://bitbucket.org/kubek2k/springockito}.
 *
 * @author Taylor Wicksell
 */
public class SpockSpyBeanPostProcessor implements BeanPostProcessor, BeanDefinitionRegistryPostProcessor {

    private final String beanName;

    public SpockSpyBeanPostProcessor(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (this.beanName.equals(beanName)) {
            return new DetachedMockFactory().createMock(beanName, bean, MockNature.SPY,
                    Collections.<String, Object>emptyMap());
        } else {
            return bean;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if(!registry.containsBeanDefinition(beanName)) {
            throw new NoSuchBeanDefinitionException(beanName, "Spock WrapWithSpy must reference an existing bean id.");
        }
    }
}
