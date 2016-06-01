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

import java.util.*;

import org.spockframework.runtime.Condition;
import org.spockframework.runtime.ConditionNotSatisfiedError;
import org.spockframework.runtime.IRunListener;
import org.spockframework.runtime.IStandardStreamsListener;
import org.spockframework.runtime.extension.builtin.UnrollNameProvider;
import org.spockframework.runtime.model.*;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.IFunction;
import org.spockframework.util.TextUtil;

import static java.util.Collections.emptyMap;
import static org.spockframework.util.CollectionUtil.*;

// NOTE: assumes single-threaded execution
// TODO: challenge assumptions that tags are added before execution, and attachments afterwards
class ReportLogEmitter implements IRunListener, IStandardStreamsListener {
  private final List<IReportLogListener> listeners = new ArrayList<IReportLogListener>();

  private SpecInfo currentSpec;
  private FeatureInfo currentFeature;
  private IterationInfo currentIteration;

  private boolean specFailed;
  private boolean featureOrIterationFailed;

  public void addListener(IReportLogListener listener) {
    listeners.add(listener);
  }

  public void standardOut(String message) {
    standardStream(message, "output");
  }

  public void standardErr(String message) {
    standardStream(message, "errorOutput");
  }

  private void standardStream(String message, String key) {
    if (currentFeature != null && !currentFeature.isParameterized()) {
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
    } else if (currentIteration != null) {
      emit(mapOf(
          "package", currentSpec.getPackage(),
          "name", currentSpec.getName(),
          "features", listOf(
          mapOf(
              "name", currentIteration.getFeature().getIterationNameProvider().getName(currentIteration),
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

  public void beforeSpec(SpecInfo spec) {
    currentSpec = spec;
    specFailed = false;

    emit(putAll(mapOf(
        "package", spec.getPackage(),
        "name", spec.getName(),
        "start", getCurrentTime()
    ), renderNarrative(spec.getNarrative()), renderTags(spec.getTags())));
  }

  public void beforeFeature(FeatureInfo feature) {
    if (!feature.isParameterized()) {
      currentFeature = feature;
      featureOrIterationFailed = false;

      emit(mapOf(
          "package", feature.getSpec().getBottomSpec().getPackage(),
          "name", feature.getSpec().getBottomSpec().getName(),
          "features", listOf(putAll(mapOf(
          "name", feature.getName(),
          "start", getCurrentTime()
      ), renderBlocks(feature.getBlocks()), renderTags(feature.getTags())))
      ));
    }
  }

  public void beforeIteration(IterationInfo iteration) {
    currentIteration = iteration;
    featureOrIterationFailed = false;

    emit(mapOf(
        "package", iteration.getFeature().getSpec().getBottomSpec().getPackage(),
        "name", iteration.getFeature().getSpec().getBottomSpec().getName(),
        "features", listOf(putAll(mapOf(
        "name", iteration.getFeature().getIterationNameProvider().getName(iteration),
        "start", getCurrentTime(),
        "data", iteration.getDataValues()
    ), renderBlocks(iteration), renderTags(iteration.getFeature().getTags())))
    ));
  }

  public void afterIteration(IterationInfo iteration) {
    emit(mapOf(
        "package", iteration.getFeature().getSpec().getBottomSpec().getPackage(),
        "name", iteration.getFeature().getSpec().getBottomSpec().getName(),
        "features", listOf(
        putAll(mapOf(
            "name", iteration.getFeature().getIterationNameProvider().getName(iteration),
            "end", getCurrentTime(),
            "result", featureOrIterationFailed ? "failed" : "passed"
        ), renderAttachments(iteration.getFeature().getAttachments()))
    )
    ));

    currentIteration = null;

  }

  public void afterFeature(FeatureInfo feature) {
    if (!feature.isParameterized()) {
      emit(mapOf(
          "package", feature.getSpec().getBottomSpec().getPackage(),
          "name", feature.getSpec().getBottomSpec().getName(),
          "features", listOf(
          putAll(mapOf(
              "name", feature.getName(),
              "end", getCurrentTime(),
              "result", featureOrIterationFailed ? "failed" : "passed"
          ), renderAttachments(feature.getAttachments()))
      )
      ));

      currentFeature = null;
    }
  }

  public void afterSpec(SpecInfo spec) {
    emit(putAll(mapOf(
        "package", spec.getPackage(),
        "name", spec.getName(),
        "end", getCurrentTime(),
        "result", specFailed ? "failed" : "passed"
    ), renderAttachments(spec.getAttachments())));

    currentSpec = null;
  }

  public void error(ErrorInfo error) {
    specFailed = true;
    SpecInfo spec = error.getMethod().getParent().getBottomSpec();
//        FeatureInfo feature = error.getMethod().getFeature();
    if (currentFeature != null) {
      featureOrIterationFailed = true;
      emit(mapOf(
          "package", spec.getPackage(),
          "name", spec.getName(),
          "features", listOf(putAll(
          mapOf(
              "name", currentFeature.getName(),
              "exceptions", listOf(ExceptionUtil.printStackTrace(error.getException()))
          ), renderBlocks(currentFeature.getBlocks(), error)
      ))
      ));
    } else if (currentIteration != null) {
      featureOrIterationFailed = true;
      emit(mapOf(
          "package", spec.getPackage(),
          "name", spec.getName(),
          "features", listOf(putAll(
          mapOf(
//              not so nice but it works
              "name", currentIteration.getFeature().getIterationNameProvider().getName(currentIteration),
              "exceptions", listOf(ExceptionUtil.printStackTrace(error.getException()))
          ), renderBlocks(currentIteration, error)
      ))
      ));
    } else {
      emit(mapOf(
          "package", spec.getPackage(),
          "name", spec.getName(),
          "exceptions", listOf(ExceptionUtil.printStackTrace(error.getException()))
      ));
    }
  }

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

  private Map renderBlocks(IterationInfo iteration) {
    List<BlockInfo> blocks = iteration.getFeature().getBlocks();
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < blocks.size(); i++) {
      BlockInfo block = blocks.get(i);
      if (block.getTexts().isEmpty()) {
        continue;
      }
      String name = block.getKind().name();
      String label = name == "SETUP" ? "Given" : TextUtil.capitalize(name.toLowerCase());
      for (int j = 0; j < block.getTexts().size(); j++) {
        String text = block.getTexts().get(j);
        if (j == 0) {
          builder.append(label);
        } else {
          builder.append("And");
        }
        builder.append(" ");
        UnrollNameProvider p = new UnrollNameProvider(iteration.getFeature(), text);
        builder.append(p.getName(iteration));
        if (i < blocks.size() - 1 || j < block.getTexts().size() - 1) {
          builder.append("\n");
        }
      }
    }

    String result = builder.toString();
    return result.length() > 0 ? mapOf("narrative", result) : emptyMap();
  }

  //    TODO duplicate code..needs to be updated
  private Map renderBlocks(IterationInfo iteration, ErrorInfo error) {
    List<BlockInfo> blocks = iteration.getFeature().getBlocks();
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < blocks.size(); i++) {
      BlockInfo block = blocks.get(i);
      if (block.getTexts().isEmpty()) {
        continue;
      }
      String name = block.getKind().name();
      String label = name == "SETUP" ? "Given" : TextUtil.capitalize(name.toLowerCase());
      for (int j = 0; j < block.getTexts().size(); j++) {
        String text = block.getTexts().get(j);
        if (j == 0) {
          builder.append(label);
        } else {
          builder.append("And");
        }
        builder.append(" ");
//       TODO probably reuse the compiled pattern
        UnrollNameProvider p = new UnrollNameProvider(iteration.getFeature(), text);
        String n = p.getName(iteration);

        if (error.getException() instanceof ConditionNotSatisfiedError) {
          ConditionNotSatisfiedError conditionNotSatisfiedError = (ConditionNotSatisfiedError) error.getException();
          Condition condition = conditionNotSatisfiedError.getCondition();
          if (condition.getPosition().getLine() >= block.getLineNumber()
              && condition.getPosition().getLine() <= block.getLastLineNumber()) {
            n = n + "->failed";
          }
        }
        builder.append(n);
        if (i < blocks.size() - 1 || j < block.getTexts().size() - 1) {
          builder.append("\n");
        }
      }
    }

    String result = builder.toString();
    return result.length() > 0 ? mapOf("narrative", result) : emptyMap();
  }

  private Map renderBlocks(List<BlockInfo> blocks, ErrorInfo error) {
    StringBuilder builder = new StringBuilder();


    for (int i = 0; i < blocks.size(); i++) {
      BlockInfo block = blocks.get(i);
      if (block.getTexts().isEmpty()) {
        continue;
      }
      String name = block.getKind().name();
      String label = name == "SETUP" ? "Given" : TextUtil.capitalize(name.toLowerCase());
      for (int j = 0; j < block.getTexts().size(); j++) {
        String text = block.getTexts().get(j);
        if (j == 0) {
          builder.append(label);
        } else {
          builder.append("And");
        }
        builder.append(" ");
        if (error.getException() instanceof ConditionNotSatisfiedError) {
          ConditionNotSatisfiedError conditionNotSatisfiedError = (ConditionNotSatisfiedError) error.getException();
          Condition condition = conditionNotSatisfiedError.getCondition();
          if (condition.getPosition().getLine() >= block.getLineNumber()
              && condition.getPosition().getLine() <= block.getLastLineNumber()) {
            text = text + "->failed";
          }
        }
        builder.append(text);

        if (i < blocks.size() - 1 || j < block.getTexts().size() - 1) {
          builder.append("\n");
        }
      }
    }

    String result = builder.toString();
    return result.length() > 0 ? mapOf("narrative", result) : emptyMap();
  }

  private Map renderBlocks(List<BlockInfo> blocks) {
    StringBuilder builder = new StringBuilder();


    for (int i = 0; i < blocks.size(); i++) {
      BlockInfo block = blocks.get(i);
      if (block.getTexts().isEmpty()) {
        continue;
      }
      String name = block.getKind().name();
      String label = name.equals("SETUP") ? "Given" : TextUtil.capitalize(name.toLowerCase());
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
}
