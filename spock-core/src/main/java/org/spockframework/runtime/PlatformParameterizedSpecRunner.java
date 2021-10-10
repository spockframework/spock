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

import org.spockframework.runtime.model.IterationInfo;
import spock.config.RunnerConfiguration;

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

    try (DataIterator dataProviderStream = new DataIteratorFactory(supervisor).createDataProviderStream(context)) {
      runIterations(context, childExecutor, dataProviderStream);
      childExecutor.awaitFinished();
    } catch (InterruptedException ie) {
      throw ie;
    } catch (Exception e) {
      // DataIterator.close doesn't throw
    }
  }

  private void runIterations(SpockExecutionContext context, ParameterizedFeatureChildExecutor childExecutor, DataIterator dataIterator) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return;
    }

    int iterationIndex = 0;
    while (dataIterator.hasNext()) {
      IterationInfo iterationInfo = createIterationInfo(context, iterationIndex, dataIterator.next(), dataIterator.getEstimatedNumIterations());
      IterationNode iterationNode = new IterationNode(
          context.getParentId().append("iteration", String.valueOf(iterationIndex++)),
          context.getRunContext().getConfiguration(RunnerConfiguration.class), iterationInfo);

      if (context.getErrorInfoCollector().hasErrors()) {
        return;
      }
      if (iterationInfo.getFeature().getIterationFilter().isAllowed(iterationInfo.getIterationIndex())) {
        childExecutor.execute(iterationNode);
      }
    }
  }
}
