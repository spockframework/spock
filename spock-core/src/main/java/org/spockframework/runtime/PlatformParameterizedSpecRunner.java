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

package org.spockframework.runtime;

import org.spockframework.runtime.extension.IDataDriver;
import org.spockframework.runtime.extension.IIterationRunner;
import org.spockframework.runtime.model.ExecutionResult;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import spock.config.RunnerConfiguration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adds the ability to run parameterized features.
 *
 * @author Peter Niederwieser
 */
public class PlatformParameterizedSpecRunner extends PlatformSpecRunner {
  public PlatformParameterizedSpecRunner(IRunSupervisor supervisor) {
    super(supervisor);
  }

  @Override
  void runParameterizedFeature(SpockExecutionContext context, ParameterizedFeatureChildExecutor childExecutor) throws InterruptedException {
    if (context.getErrorInfoCollector().hasErrors()) {
      return;
    }

    FeatureInfo feature = context.getCurrentFeature();
    try (DataIterator dataIterator = new DataIteratorFactory(supervisor).createFeatureDataIterator(context)) {
      IIterationRunner iterationRunner = createIterationRunner(context, childExecutor);
      IDataDriver dataDriver = feature.getDataDriver();
      dataDriver.runIterations(dataIterator, iterationRunner, feature.getFeatureMethod().getParameters());
      childExecutor.awaitFinished();
    } catch (InterruptedException ie) {
      throw ie;
    } catch (Exception e) {
      // DataIterator.close doesn't throw
    }
  }

  private IIterationRunner createIterationRunner(SpockExecutionContext context, ParameterizedFeatureChildExecutor childExecutor) {
    return new IIterationRunner() {
      private final AtomicInteger iterationIndex = new AtomicInteger(0);

      @Override
      public CompletableFuture<ExecutionResult> runIteration(Object[] args) {
        IterationInfo iterationInfo = createIterationInfo(context, iterationIndex.get(), args, -1);
        IterationNode iterationNode = new IterationNode(
          context.getParentId().append("iteration", String.valueOf(iterationIndex.getAndIncrement())),
          context.getRunContext().getConfiguration(RunnerConfiguration.class), iterationInfo);

        if (context.getErrorInfoCollector().hasErrors()) {
          return CompletableFuture.completedFuture(ExecutionResult.REJECTED);
        }
        if (iterationInfo.getFeature().getIterationFilter().isAllowed(iterationInfo.getIterationIndex())) {
          return childExecutor.execute(iterationNode);
        }
        return CompletableFuture.completedFuture(ExecutionResult.SKIPPED);
      }
    };
  }
}
