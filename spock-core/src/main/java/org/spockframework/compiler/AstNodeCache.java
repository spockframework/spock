/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.compiler;

import org.codehaus.groovy.ast.*;

import org.spockframework.runtime.SpockRuntime;
import org.spockframework.runtime.ValueRecorder;
import org.spockframework.runtime.model.*;

import org.spockframework.util.Identifiers;
import spock.lang.*;

/**
 * Provides access to frequently used AST nodes.
 *
 * @author Peter Niederwieser
 */

public class AstNodeCache {
  public final ClassNode SpockRuntime = ClassHelper.makeWithoutCaching(SpockRuntime.class);
  public final ClassNode ValueRecorder = ClassHelper.makeWithoutCaching(ValueRecorder.class);
  public final ClassNode ErrorCollector = ClassHelper.makeWithoutCaching(org.spockframework.runtime.ErrorCollector.class);
  public final ClassNode Specification = ClassHelper.makeWithoutCaching(Specification.class);
  public final ClassNode SpecInternals = ClassHelper.makeWithoutCaching(org.spockframework.lang.SpecInternals.class);

  public final MethodNode SpecInternals_GetSpecificationContext =
      SpecInternals.getDeclaredMethods(Identifiers.GET_SPECIFICATION_CONTEXT).get(0);

  public final MethodNode SpockRuntime_VerifyCondition =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.VERIFY_CONDITION).get(0);

  public final MethodNode SpockRuntime_ConditionFailedWithException =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.CONDITION_FAILED_WITH_EXCEPTION).get(0);

  public final MethodNode SpockRuntime_VerifyMethodCondition =
      SpockRuntime.getDeclaredMethods(org.spockframework.runtime.SpockRuntime.VERIFY_METHOD_CONDITION).get(0);

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

  // annotations and annotation elements
  public final ClassNode SpecMetadata = ClassHelper.makeWithoutCaching(SpecMetadata.class);
  public final ClassNode FieldMetadata = ClassHelper.makeWithoutCaching(FieldMetadata.class);
  public final ClassNode FeatureMetadata = ClassHelper.makeWithoutCaching(FeatureMetadata.class);
  public final ClassNode DataProviderMetadata = ClassHelper.makeWithoutCaching(DataProviderMetadata.class);
  public final ClassNode BlockMetadata = ClassHelper.makeWithoutCaching(BlockMetadata.class);
  public final ClassNode BlockKind = ClassHelper.makeWithoutCaching(BlockKind.class);

  // mocking API
  public final ClassNode InteractionBuilder = ClassHelper.makeWithoutCaching(org.spockframework.mock.runtime.InteractionBuilder.class);

  // external types
  public final ClassNode Throwable = ClassHelper.makeWithoutCaching(Throwable.class);
}
