/*
 * Copyright 2009, 2011 the original author or authors.
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

package org.spockframework.tapestry;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceLifecycle2;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;

/**
 * Allows a service to exist per feature iteration.  This is necessary because a specification
 * may involve tests that span multiple threads.
 *
 */
@SuppressWarnings("unchecked")
public class PerIterationServiceLifecycle implements ServiceLifecycle2 {

    private final IPerIterationManager manager;

    private final PlasticProxyFactory proxyFactory;

    private static class PerIterationObjectCreator implements ObjectCreator {

        private final PerIterationValue value;

        private final ObjectCreator delegate;

        private PerIterationObjectCreator(PerIterationValue value, ObjectCreator delegate) {
            this.value = value;
            this.delegate = delegate;
        }

        @Override
        public Object createObject() {
            Object result = value.get();

            if (result == null) {
                result = delegate.createObject();
                value.set(result);
            }

            return result;
        }
    }

    public PerIterationServiceLifecycle(@SpockTapestry
                                        IPerIterationManager manager,

                                        @Builtin
                                        PlasticProxyFactory proxyFactory) {
        this.manager = manager;
        this.proxyFactory = proxyFactory;
    }

    /**
     * Returns false; this lifecycle represents a service that will be created many times.
     */
    public boolean isSingleton() {
        return false;
    }

    public Object createService(ServiceResources resources, ObjectCreator creator) {
        ObjectCreator perIterationCreator = new PerIterationObjectCreator(manager.createValue(), creator);

        Class serviceInterface = resources.getServiceInterface();

        return proxyFactory.createProxy(serviceInterface, perIterationCreator, String.format("<PerIteration Proxy for %s(%s)>", resources.getServiceId(), serviceInterface.getName()));
    }

    @Override
    public boolean requiresProxy() {
        return true;
    }
}
