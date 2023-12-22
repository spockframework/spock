/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package spock.lang;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.spockframework.runtime.Condition;
import org.spockframework.runtime.ConditionNotSatisfiedError;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.TextPosition;
import org.spockframework.util.IoUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Snapshotter {
  private final Store snapshotStore;
  private Wrapper wrapper = Wrapper.NOOP;

  public Snapshotter(Store snapshotStore) {
    this.snapshotStore = snapshotStore;
  }

  protected Store getSnapshotStore() {
    return snapshotStore;
  }

  protected String loadSnapshot() {
    return getSnapshotStore().loadSnapshot().map(wrapper::unwrap).orElse("<Missing Snapshot>");
  }

  protected void saveSnapshot(String value) {
    snapshotStore.saveSnapshot(wrapper.wrap(value));
  }

  /**
   * Declares a {@link Wrapper} for wrapping and unwrapping of the reference value.
   * <P>
   * It is suggested that users create subclasses of {@link Snapshotter} with common wrapped types,
   * instead of using this method directly.
   */
  public Snapshotter wrappedAs(Wrapper wrapper) {
    this.wrapper = wrapper;
    return this;
  }

  /**
   * Specifies the {@code actual} value.
   * <p>
   * Note: The value is normalized to {@code \n} line endings.
   * @param value the actual value for assertions
   */
  public Snap assertThat(String value) {
    return new Snap(value);
  }

  public class Snap {
    private final String value;

    private Snap(String value) {
      this.value = StringGroovyMethods.normalize(value);
    }

    public void matchesSnapshot() {
      String snapshotValue = loadSnapshot();
      if (!Objects.equals(value, snapshotValue)) {
        if (snapshotStore.isUpdateSnapshots()) {
          saveSnapshot(value);
        } else {
          // manually construct a ConditionNotSatisfiedError, as the native groovy assert doesn't get properly rendered by Intellij
          throw new ConditionNotSatisfiedError(new Condition(Arrays.asList(value, snapshotValue), "value == snapshotValue", TextPosition.create(-1, -1), null, null, null));
        }
      }
    }
  }

  public interface Wrapper {
    Wrapper NOOP = new NoopWrapper();

    String wrap(String string);

    String unwrap(String string);
  }

  public static class PrefixSuffixWrapper implements Wrapper {

    private final String prefix;
    private final String suffix;

    private PrefixSuffixWrapper(String prefix, String suffix) {
      this.prefix = prefix;
      this.suffix = suffix;
    }

    public static PrefixSuffixWrapper of(String prefix, String suffix) {
      return new PrefixSuffixWrapper(prefix, suffix);
    }

    public static PrefixSuffixWrapper asciiDocSample(String prefix, String suffix) {
      return new PrefixSuffixWrapper(prefix + "\n/*--------- tag::snapshot[] ---------*/\n", "\n/*--------- end::snapshot[] ---------*/\n" + suffix);
    }

    @Override
    public String wrap(String value) {
      return prefix + value + suffix;
    }

    @Override
    public String unwrap(String value) {
      // check if unwrapping is possible
      if (!value.startsWith(prefix) || !value.endsWith(suffix)) {
        System.err.printf("Cannot unwrap because prefix matches=%s, prefix matches=%s%n", value.startsWith(prefix),value.endsWith(suffix));
        return value;
      }
      return value.substring(prefix.length(), value.length() - suffix.length());
    }
  }

  private static class NoopWrapper implements Wrapper {

    @Override
    public String wrap(String string) {
      return string;
    }

    @Override
    public String unwrap(String string) {
      return string;
    }
  }

  public static final class Store {
    private final boolean updateSnapshots;
    private final Charset charset;
    private final Path snapshotPath;

    public Store(IterationInfo iterationInfo, Path rootPath, boolean updateSnapshots, String extension, Charset charset) {
      this.updateSnapshots = updateSnapshots;
      this.charset = charset;

      Class<?> specClass = iterationInfo.getFeature().getSpec().getReflection();
      Path specPath = rootPath
        .resolve(specClass.getPackage().getName().replace('.', '/'))
        .resolve(specClass.getSimpleName().replace('$', '/')); // use subdirectories for inner classes
      String uniqueName = calculateSafeUniqueName(extension, iterationInfo);
      snapshotPath = specPath.resolve(uniqueName);
    }

    private static String calculateSafeUniqueName(String extension, IterationInfo iterationInfo) {
      FeatureInfo feature = iterationInfo.getFeature();
      String safeName = feature.getName().replaceAll("[^a-zA-Z0-9]", "_");
      if (safeName.length() > 240) {
        safeName = safeName.substring(0, 240) + "_" + (feature.getFeatureMethod().getReflection().getName().substring("$spock_feature_".length()));
      }
      String iterationIndex = feature.isParameterized() ? String.format("_[%d]", iterationInfo.getIterationIndex()) : "";
      return String.format("%s%s.%s", safeName, iterationIndex, extension);
    }

    public Optional<String> loadSnapshot() {
      if (Files.exists(snapshotPath)) {
        try {
          return Optional.of(IoUtil.getText(snapshotPath, charset));
        } catch (IOException e) {
          throw new UncheckedIOException("Failure while trying to load Snapshot: " + snapshotPath, e);
        }
      }
      return Optional.empty();
    }


    public void saveSnapshot(String value) {
      snapshotPath.getParent().toFile().mkdirs();
      IoUtil.writeText(snapshotPath, value, charset);
    }

    public boolean isUpdateSnapshots() {
      return updateSnapshots;
    }
  }
}
