package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.opentest4j.AssertionFailedError
import org.spockframework.runtime.Condition
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.TextPosition

import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
class Snapshotter {
  private final IterationInfo iterationInfo
  private final boolean updateSnapshots
  private final Path snapshotPath
  private SnapshotWrapper wrapper = SnapshotWrapper.NOOP

  @PackageScope
  Snapshotter(IterationInfo iterationInfo, Path rootPath, boolean updateSnapshots, String extension) {
    this.iterationInfo = iterationInfo
    this.updateSnapshots = updateSnapshots
    Path specPath = rootPath.resolve(iterationInfo.feature.spec.reflection.package.name.replace('.', '/'))
    String safeName = "${iterationInfo.feature.spec.name}__${iterationInfo.feature.name}".replaceAll('[^a-zA-Z0-9]', '_').tap {
      it.size() > 100
        ? it.substring(0, 100) + "_${iterationInfo.feature.featureMethod.reflection.name - '$spock_feature_'}"
        : it
    }
    String iterationIndex = iterationInfo.feature.isParameterized() ? "_[$iterationInfo.iterationIndex]" : ""
    String uniqueName = "${safeName}${iterationIndex}.${extension}"
    snapshotPath = specPath.resolve(uniqueName)
  }

  private String loadSnapshot() {
    if (Files.exists(snapshotPath)) {
      wrapper.unwrap(snapshotPath.text)
    } else {
      "<Missing Snapshot>"
    }
  }

  Snapshotter specBody() {
    wrapper = SnapshotWrapper.SPEC_BODY
    this
  }

  Snapshotter featureBody() {
    wrapper = SnapshotWrapper.FEATURE_BODY
    this
  }

  private void saveSnapshot(String value) {
    snapshotPath.parent.toFile().mkdirs()
    snapshotPath.text = wrapper.wrap(value)
    System.err.println("Updated snapshot for: ${iterationInfo.feature.spec.name}.${iterationInfo.feature.name}")
  }

  Snap assertThat(String value) {
    new Snap(value)
  }

  @CompileStatic
  class Snap {
    private String value

    private Snap(String value) {
      this.value = value
    }

    void matchesSnapshot() {
      String snapshotValue = loadSnapshot()
      if (!Objects.equals(value, snapshotValue)) {
        if (updateSnapshots) {
          saveSnapshot(value)
        } else {
          // manually construct a ConditionNotSatisfiedError, as the native groovy assert doesn't get properly rendered by Intellij
          throw new ConditionNotSatisfiedError(
            new Condition([value, snapshotValue] as List<Object>, 'value == snapshotValue', TextPosition.create(-1, -1), null, null, null)
          )
        }
      }
    }
  }
}
