/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.compiler;

import org.codehaus.groovy.runtime.wrappers.PojoWrapper;
import org.spockframework.mock.runtime.MockController;
import org.spockframework.runtime.*;
import org.spockframework.runtime.extension.RepeatedExtensionAnnotations;
import org.spockframework.runtime.model.*;
import org.spockframework.util.Identifiers;
import spock.lang.Specification;

import org.codehaus.groovy.ast.*;

import java.util.Set;

/**
 * Provides access to frequently used AST nodes.
 *
 * @author Peter Niederwieser
 */

public class AstNodeCache {
  public final ClassNode SpockRuntime = ClassHelper.makeWithoutCaching(SpockRuntime.class);
  public final ClassNode ValueRecorder = ClassHelper.makeWithoutCaching(ValueRecorder.class);
  public final ClassNode ErrorCollector = ClassHelper.makeWithoutCaching(ErrorCollector.class);
  public final ClassNode ErrorRethrower = ClassHelper.makeWithoutCaching(ErrorRethrower.class);
  public final ClassNode Specification = ClassHelper.makeWithoutCaching(Specification.class);
  public final ClassNode SpecInternals = ClassHelper.makeWithoutCaching(SpecInternals.class);
  public final ClassNode MockController = ClassHelper.makeWithoutCaching(MockController.class);
  public final ClassNode SpecificationContext = ClassHelper.makeWithoutCaching(SpecificationContext.class);
  public final ClassNode DataVariableMultiplication = ClassHelper.makeWithoutCaching(DataVariableMultiplication.class);
  public final ClassNode DataVariableMultiplicationFactor = ClassHelper.makeWithoutCaching(DataVariableMultiplicationFactor.class);
  public final ClassNode BlockInfo = ClassHelper.makeWithoutCaching(BlockInfo.class);

  public final MethodNode Specification_GetSpecificationContext =
    Specification.getDeclaredMethods(Identifiers.GET_SPECIFICATION_CONTEXT).get(0);

  public final MethodNode SpockRuntime_VerifyCondition =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.VERIFY_CONDITION).get(0);

  public final MethodNode SpockRuntime_ConditionFailedWithException =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.CONDITION_FAILED_WITH_EXCEPTION).get(0);

  public final MethodNode SpockRuntime_GroupConditionFailedWithException =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.GROUP_CONDITION_FAILED_WITH_EXCEPTION).get(0);

  public final MethodNode SpockRuntime_VerifyMethodCondition =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.VERIFY_METHOD_CONDITION).get(0);

  public final MethodNode SpockRuntime_MatchCollectionsAsSet =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.MATCH_COLLECTIONS_AS_SET).get(0);

  public final MethodNode SpockRuntime_MatchCollectionsInAnyOrder =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.MATCH_COLLECTIONS_IN_ANY_ORDER).get(0);

  public final MethodNode SpockRuntime_DespreadList =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.DESPREAD_LIST).get(0);

  public final MethodNode SpockRuntime_CallBlockEntered =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.CALL_BLOCK_ENTERED).get(0);

  public final MethodNode SpockRuntime_CallBlockExited =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.CALL_BLOCK_EXITED).get(0);

  public final MethodNode ValueRecorder_Reset =
      ValueRecorder.getDeclaredMethods(org.spockframework.runtime.ValueRecorder.RESET).get(0);

  public final MethodNode ValueRecorder_Record =
      ValueRecorder.getDeclaredMethods(org.spockframework.runtime.ValueRecorder.RECORD).get(0);

  public final MethodNode ValueRecorder_StartRecordingValue =
      ValueRecorder.getDeclaredMethods(org.spockframework.runtime.ValueRecorder.START_RECORDING_VALUE).get(0);

  public final MethodNode ValueRecorder_RealizeNas =
      ValueRecorder.getDeclaredMethods(org.spockframework.runtime.ValueRecorder.REALIZE_NAS).get(0);

  public final MethodNode ErrorCollector_Validate =
      ErrorCollector.getDeclaredMethods(org.spockframework.runtime.ErrorCollector.VALIDATE_COLLECTED_ERRORS).get(0);

  public final MethodNode MockController_AddInteraction =
      MockController.getDeclaredMethods(org.spockframework.mock.runtime.MockController.ADD_INTERACTION).get(0);

  public final MethodNode MockController_EnterScope =
      MockController.getDeclaredMethods(org.spockframework.mock.runtime.MockController.ENTER_SCOPE).get(0);

  public final MethodNode MockController_AddBarrier =
      MockController.getDeclaredMethods(org.spockframework.mock.runtime.MockController.ADD_BARRIER).get(0);

  public final MethodNode MockController_LeaveScope =
      MockController.getDeclaredMethods(org.spockframework.mock.runtime.MockController.LEAVE_SCOPE).get(0);

  public final MethodNode SpecificationContext_GetMockController =
      SpecificationContext.getDeclaredMethods(org.spockframework.runtime.SpecificationContext.GET_MOCK_CONTROLLER).get(0);

  public final MethodNode SpecificationContext_SetThrownException =
      SpecificationContext.getDeclaredMethods(org.spockframework.runtime.SpecificationContext.SET_THROWN_EXCEPTION).get(0);

  public final MethodNode SpecificationContext_GetSharedInstance =
      SpecificationContext.getDeclaredMethods(org.spockframework.runtime.SpecificationContext.GET_SHARED_INSTANCE).get(0);

  public final MethodNode SpecificationContext_GetCurrentBlock =
      SpecificationContext.getDeclaredMethods(org.spockframework.runtime.SpecificationContext.GET_CURRENT_BLOCK).get(0);

  public final MethodNode SpecificationContext_SetCurrentBlock =
      SpecificationContext.getDeclaredMethods(org.spockframework.runtime.SpecificationContext.SET_CURRENT_BLOCK).get(0);

  public final MethodNode List_Get =
      ClassHelper.LIST_TYPE.getDeclaredMethods("get").get(0);

  public final MethodNode Class_IsInstance =
      ClassHelper.CLASS_Type.getDeclaredMethods("isInstance").get(0);

  public final MethodNode Closure_Call =
      ClassHelper.CLOSURE_TYPE.getDeclaredMethod("call", Parameter.EMPTY_ARRAY);

  // annotations and annotation elements
  public final ClassNode RepeatedExtensionAnnotations = ClassHelper.makeWithoutCaching(RepeatedExtensionAnnotations.class);
  public final ClassNode SpecMetadata = ClassHelper.makeWithoutCaching(SpecMetadata.class);
  public final ClassNode FieldMetadata = ClassHelper.makeWithoutCaching(FieldMetadata.class);
  public final ClassNode FeatureMetadata = ClassHelper.makeWithoutCaching(FeatureMetadata.class);
  public final ClassNode DataProviderMetadata = ClassHelper.makeWithoutCaching(DataProviderMetadata.class);
  public final ClassNode DataProcessorMetadata = ClassHelper.makeWithoutCaching(DataProcessorMetadata.class);
  public final ClassNode BlockMetadata = ClassHelper.makeWithoutCaching(BlockMetadata.class);
  public final ClassNode BlockKind = ClassHelper.makeWithoutCaching(BlockKind.class);

  // mocking API
  public final ClassNode InteractionBuilder = ClassHelper.makeWithoutCaching(org.spockframework.mock.runtime.InteractionBuilder.class);

  public final MethodNode InteractionBuilder_SetRangeCount =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.SET_RANGE_COUNT).get(0);
  public final MethodNode InteractionBuilder_SetFixedCount =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.SET_FIXED_COUNT).get(0);
  public final MethodNode InteractionBuilder_AddEqualTarget =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_EQUAL_TARGET).get(0);
  public final MethodNode InteractionBuilder_AddWildcardTarget =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_WILDCARD_TARGET).get(0);
  public final MethodNode InteractionBuilder_AddEqualMethodName =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_EQUAL_METHOD_NAME).get(0);
  public final MethodNode InteractionBuilder_AddRegexMethodName =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_REGEX_METHOD_NAME).get(0);
  public final MethodNode InteractionBuilder_AddEqualPropertyName =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_EQUAL_PROPERTY_NAME).get(0);
  public final MethodNode InteractionBuilder_AddRegexPropertyName =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_REGEX_PROPERTY_NAME).get(0);
  public final MethodNode InteractionBuilder_SetArgListKind_boolean_boolean =
    InteractionBuilder.getDeclaredMethod(org.spockframework.mock.runtime.InteractionBuilder.SET_ARG_LIST_KIND, new Parameter[] {
      new Parameter(ClassHelper.boolean_TYPE, "isPositional"),
      new Parameter(ClassHelper.boolean_TYPE, "isMixed")
    });
  public final MethodNode InteractionBuilder_AddArgName =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_ARG_NAME).get(0);
  public final MethodNode InteractionBuilder_NegateLastArg =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.NEGATE_LAST_ARG).get(0);
  public final MethodNode InteractionBuilder_TypeLastArg =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.TYPE_LAST_ARG).get(0);
  public final MethodNode InteractionBuilder_AddCodeArg =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_CODE_ARG).get(0);
  public final MethodNode InteractionBuilder_AddEqualArg =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_EQUAL_ARG).get(0);
  public final MethodNode InteractionBuilder_AddIterableResponse =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_ITERABLE_RESPONSE).get(0);
  public final MethodNode InteractionBuilder_AddCodeResponse =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_CODE_RESPONSE).get(0);
  public final MethodNode InteractionBuilder_AddConstantResponse =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.ADD_CONSTANT_RESPONSE).get(0);
  public final MethodNode InteractionBuilder_Build =
    InteractionBuilder.getDeclaredMethods(org.spockframework.mock.runtime.InteractionBuilder.BUILD).get(0);

  // external types
  public final ClassNode Throwable = ClassHelper.makeWithoutCaching(Throwable.class);
  public final ClassNode PojoWrapper = ClassHelper.makeWithoutCaching(PojoWrapper.class);
  public final ClassNode Set = ClassHelper.makeWithoutCaching(Set.class);

  public final MethodNode Throwable_AddSuppressed =
    Throwable.getDeclaredMethods("addSuppressed").get(0);
}
