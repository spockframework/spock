/*
 * Copyright 2024 the original author or authors.
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

package org.spockframework.mock.runtime;

import org.junit.platform.commons.support.ModifierSupport;
import org.spockframework.mock.IInvocationConstraint;
import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.MockImplementation;
import org.spockframework.mock.constraint.EqualMethodNameConstraint;
import org.spockframework.mock.constraint.EqualPropertyNameConstraint;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.util.ThreadSafe;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.spockframework.runtime.GroovyRuntimeUtil.propertyToBooleanGetterMethodName;
import static org.spockframework.runtime.GroovyRuntimeUtil.propertyToGetterMethodName;

/**
 * {@link ByteBuddyMockInteractionValidator} validates the {@link IMockInteraction} for {@link ByteBuddyMockMaker} mocks.
 *
 * <ul>
 *   <li>Checks for method interactions on final methods</li>
 *   <li>Checks for property interactions on final methods</li>
 * </ul>
 *
 * <p>Implementation note: The {@link ByteBuddyMockFactory} create a single instance for each mocked class
 * and the same validation is used for multiple mocks of the same class.
 *
 * @author Andreas Turban
 */
@ThreadSafe
final class ByteBuddyMockInteractionValidator implements IMockInteractionValidator {

  private volatile Class<?> mockClass;
  private volatile Set<String> finalMethods;

  ByteBuddyMockInteractionValidator() {
  }

  @Override
  public void validateMockInteraction(IMockObject mockObject, IMockInteraction mockInteractionParam) {
    requireNonNull(mockObject);
    requireNonNull(mockInteractionParam);
    Object instance = requireNonNull(mockObject.getInstance());

    if (mockObject.getConfiguration().getImplementation() == MockImplementation.GROOVY) {
      //We do not validate final methods for Groovy mocks, because final mocking can be done with the Groovy MOP.
      return;
    }

    initializeClassData(instance);

    validate(mockObject, (MockInteraction) mockInteractionParam);
  }

  private void validate(IMockObject mockObject, MockInteraction mockInteraction) {
    for (IInvocationConstraint constraint : mockInteraction.getConstraints()) {
      if (constraint instanceof EqualMethodNameConstraint) {
        EqualMethodNameConstraint methodConstraint = (EqualMethodNameConstraint) constraint;
        String methodName = methodConstraint.getMethodName();
        validateNonFinalMethod(mockObject, methodName);
      }
      if (constraint instanceof EqualPropertyNameConstraint) {
        EqualPropertyNameConstraint propNameConstraint = (EqualPropertyNameConstraint) constraint;
        validateProperty(mockObject, propNameConstraint);
      }
    }
  }

  private void validateProperty(IMockObject mockObject, EqualPropertyNameConstraint propNameConstraint) {
    String propName = propNameConstraint.getPropertyName();
    //We do not need to check for setters, because a property access like x.prop = value has not mock interaction syntax
    validateNonFinalMethod(mockObject, propertyToGetterMethodName(propName));
    validateNonFinalMethod(mockObject, propertyToBooleanGetterMethodName(propName));
  }

  private void validateNonFinalMethod(IMockObject mockObject, String methodName) {
    if (finalMethods.contains(methodName)) {
      throw new InvalidSpecException("The final method '"
        + methodName + "' of '"
        + mockObject.getMockName()
        + "' can't be mocked by the '"
        + ByteBuddyMockMaker.ID +
        "' mock maker. Please use another mock maker supporting final methods.");
    }
  }

  private void initializeClassData(Object mock) {
    Class<?> mockClassOfMockObj = mock.getClass();
    if (mockClass == null) {
      synchronized (this) {
        if (mockClass == null) {
          finalMethods = Arrays.stream(mockClassOfMockObj.getMethods())
            .filter(m -> !ModifierSupport.isStatic(m))
            .collect(Collectors.groupingBy(Method::getName))
            .entrySet()
            .stream()
            .filter(e -> e.getValue()
              .stream()
              .allMatch(ModifierSupport::isFinal))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

          mockClass = mockClassOfMockObj;
        }
      }
    }

    if (mockClass != mockClassOfMockObj) {
      throw new IllegalStateException();
    }
  }
}
