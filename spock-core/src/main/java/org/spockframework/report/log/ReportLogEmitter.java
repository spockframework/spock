/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report.log;

import org.spockframework.runtime.*;
import org.spockframework.runtime.model.*;
import org.spockframework.util.*;

import java.util.*;

import static java.util.Collections.emptyMap;
import static org.spockframework.util.CollectionUtil.*;

// NOTE: assumes single-threaded execution
// TODO: challenge assumptions that tags are added before execution, and attachments afterwards
class ReportLogEmitter implements IRunListener, IStandardStreamsListener {
  private final List<IReportLogListener> listeners = new ArrayList<>();

  private SpecInfo currentSpec;
  private FeatureInfo currentFeature;
  private IterationInfo currentIteration;

  private boolean specFailed;
  private boolean featureFailed;
  private boolean iterationFailed;

  public void addListener(IReportLogListener listener) {
    listeners.add(listener);
  }

  @Override
  public void standardOut(String message) {
    standardStream(message, "output");
  }

  @Override
  public void standardErr(String message) {
    standardStream(message, "errorOutput");
  }

  private void standardStream(String message, String key) {
    if (currentIteration != null && reportIterations(currentIteration.getFeature())) {
      emit(mapOf(
        "package", currentSpec.getPackage(),
        "name", currentSpec.getName(),
        "features", listOf(mapOf(
            "name", currentFeature.getName(),
            "iterations", listOf(mapOf(
              "name", currentIteration.getName(),
                key, listOf(message)
            ))
          ))
      ));
    } else if (currentFeature != null) {
      emit(mapOf(
          "package", currentSpec.getPackage(),
          "name", currentSpec.getName(),
          "features", listOf(
          mapOf(
              "name", currentFeature.getName(),
              key, listOf(message)
          )
      )
      ));
    } else if (currentSpec != null) {
      emit(mapOf(
          "package", currentSpec.getPackage(),
          "name", currentSpec.getName(),
          key, listOf(message)
      ));
    }
  }

  @Override
  public void beforeSpec(SpecInfo spec) {
    currentSpec = spec;
    specFailed = false;

    emit(putAll(mapOf(
        "package", spec.getPackage(),
        "name", spec.getName(),
        "start", getCurrentTime()
    ), renderNarrative(spec.getNarrative()), renderTags(spec.getTags())));
  }

  @Override
  public void beforeFeature(FeatureInfo feature) {
    currentFeature = feature;
    featureFailed = false;

    emit(mapOf(
        "package", feature.getSpec().getBottomSpec().getPackage(),
        "name", feature.getSpec().getBottomSpec().getName(),
        "features", listOf(putAll(mapOf(
        "name", feature.getName(),
        "start", getCurrentTime()
    ), renderBlocks(feature.getBlocks()), renderTags(feature.getTags())))
    ));
  }

  @Override
  public void beforeIteration(IterationInfo iteration) {
    if(reportIterations(iteration.getFeature())) {
      currentIteration = iteration;
      iterationFailed = false;

      emit(mapOf(
        "package", iteration.getFeature().getSpec().getBottomSpec().getPackage(),
        "name", iteration.getFeature().getSpec().getBottomSpec().getName(),
        "features", listOf(mapOf(
          "name", iteration.getFeature().getName(),
          "iterations", listOf(mapOf(
            "name", iteration.getName(),
            "start", getCurrentTime()
            )
          )
        ))
      ));
    }
  }

  @Override
  public void afterIteration(IterationInfo iteration) {
    if(reportIterations(iteration.getFeature())) {
      emit(mapOf(
        "package", iteration.getFeature().getSpec().getBottomSpec().getPackage(),
        "name", iteration.getFeature().getSpec().getBottomSpec().getName(),
        "features", listOf(mapOf(
          "name", iteration.getFeature().getName(),
          "iterations", listOf(mapOf(
            "name", iteration.getName(),
            "end", getCurrentTime(),
            "result", getResultSting(iterationFailed)
            )
          )
        ))
      ));
    }
    currentIteration = null;
  }

  @Override
  public void afterFeature(FeatureInfo feature) {
    emit(mapOf(
        "package", feature.getSpec().getBottomSpec().getPackage(),
        "name", feature.getSpec().getBottomSpec().getName(),
        "features", listOf(
        putAll(mapOf(
            "name", feature.getName(),
            "end", getCurrentTime(),
            "result", getResultSting(featureFailed)
        ), renderAttachments(feature.getAttachments()))
    )
    ));

    currentFeature = null;
  }

  @Override
  public void afterSpec(SpecInfo spec) {
    emit(putAll(mapOf(
        "package", spec.getPackage(),
        "name", spec.getName(),
        "end", getCurrentTime(),
        "result", getResultSting(specFailed)
    ), renderAttachments(spec.getAttachments())));

    currentSpec = null;
  }

  @Override
  public void error(ErrorInfo error) {
    specFailed = true;
    SpecInfo spec = error.getMethod().getParent().getBottomSpec();
    FeatureInfo feature = error.getMethod().getFeature();
    if (feature != null) {
      featureFailed = true;
      IterationInfo iteration = error.getMethod().getIteration();
      if (iteration != null && reportIterations(feature)) {
        iterationFailed = true;
        emit(mapOf(
          "package", spec.getPackage(),
          "name", spec.getName(),
          "features", listOf(mapOf(
              "name", feature.getName(),
              "iterations", listOf(mapOf(
                "name", iteration.getName(),
                "exceptions", listOf(ExceptionUtil.printStackTrace(error.getException()))
                ))
            ))
        ));
      } else {
        emit(mapOf(
          "package", spec.getPackage(),
          "name", spec.getName(),
          "features", listOf(
            mapOf(
              "name", feature.getName(),
              "exceptions", listOf(ExceptionUtil.printStackTrace(error.getException()))
            )
          )
        ));
      }
    } else {
      emit(mapOf(
          "package", spec.getPackage(),
          "name", spec.getName(),
          "exceptions", listOf(ExceptionUtil.printStackTrace(error.getException()))
      ));
    }
  }

  @Override
  public void specSkipped(SpecInfo spec) {
    long now = getCurrentTime();

    emit(putAll(mapOf(
        "package", spec.getPackage(),
        "name", spec.getName(),
        "start", now,
        "end", now,
        "result", "skipped"
    ), renderNarrative(spec.getNarrative()), renderTags(spec.getTags())));
  }

  @Override
  public void featureSkipped(FeatureInfo feature) {
    long now = getCurrentTime();

    emit(mapOf(
        "package", feature.getSpec().getBottomSpec().getPackage(),
        "name", feature.getSpec().getBottomSpec().getName(),
        "features", listOf(putAll(mapOf(
        "name", feature.getName(),
        "start", now,
        "end", now,
        "result", "skipped"
    ), renderBlocks(feature.getBlocks()), renderTags(feature.getTags())))
    ));
  }

  // TODO: make start/end time part of the model
  // (can't determine it here because it's an async listener)
  protected long getCurrentTime() {
    return System.currentTimeMillis();
  }

  private Map renderTags(List<Tag> tags) {
    if (tags.isEmpty()) return emptyMap();

    List result = filterMap(tags, new IFunction<Tag, Object>() {
      @Override
      public Object apply(Tag tag) {
        return filterNullValues(mapOf(
            "name", tag.getName(),
            "key", tag.getKey(),
            "value", tag.getValue(),
            "url", tag.getUrl()
        ));
      }
    });
    return mapOf("tags", result);
  }

  private Map renderNarrative(String narrative) {
    return narrative != null ? mapOf("narrative", narrative) : emptyMap();
  }

  private Map renderBlocks(List<BlockInfo> blocks) {
    StringBuilder builder = new StringBuilder();


    for (int i = 0; i < blocks.size(); i++) {
      BlockInfo block = blocks.get(i);
      if (block.getTexts().isEmpty()) {
        continue;
      }
      String name = block.getKind().name();
      String label = "SETUP".equals(name) ? "Given" : TextUtil.capitalize(name.toLowerCase());
      for (int j = 0; j < block.getTexts().size(); j++) {
        String text = block.getTexts().get(j);
        if (j == 0) {
          builder.append(label);
        } else {
          builder.append("And");
        }
        builder.append(" ");
        builder.append(text);
        if (i < blocks.size() - 1 || j < block.getTexts().size() - 1) {
          builder.append("\n");
        }
      }
    }

    String result = builder.toString();
    return result.length() > 0 ? mapOf("narrative", result) : emptyMap();
  }

  private Map renderAttachments(List<Attachment> attachments) {
    List result = filterMap(attachments, new IFunction<Attachment, Object>() {
      @Override
      public Object apply(Attachment attachment) {
        return mapOf("name", attachment.getName(), "url", attachment.getUrl());
      }
    });
    return result != null ? mapOf("attachments", result) : emptyMap();
  }

  private void emit(Map log) {
    for (IReportLogListener listener : listeners) {
      listener.emitted(log);
    }
  }

  private String getResultSting(boolean failed) {
    return failed ? "failed" : "passed";
  }

  private boolean reportIterations(FeatureInfo feature) {
    return feature.isParameterized() && feature.isReportIterations();
  }
}
