/*
 * Copyright 2024 the original author or authors.
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
import org.spockframework.util.Assert;
import org.spockframework.util.Beta;
import org.spockframework.util.Checks;
import org.spockframework.util.IoUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Allows to perform snapshot testing.
 * <p>
 * Snapshots are stored in a file in the configured {@link org.spockframework.runtime.extension.builtin.SnapshotConfig#rootPath}
 * with the same package structure as the specification class and grouped by the specification.
 *
 * @author Leonard Brünings
 * @since 2.4
 */
@Beta
public class Snapshotter {
  private final Store snapshotStore;
  private Wrapper wrapper = Wrapper.NOOP;

  private Function<String, String> normalizer = StringGroovyMethods::normalize;

  public Snapshotter(Store snapshotStore) {
    this.snapshotStore = Checks.notNull(snapshotStore, () -> "snapshotStore is null");
  }

  protected Store getSnapshotStore() {
    return snapshotStore;
  }

  protected String loadSnapshot(String snapshotId) {
    Checks.notNull(snapshotId, () -> "snapshotId is null");
    return getSnapshotStore().loadSnapshot(snapshotId).map(wrapper::unwrap).orElse("<Missing Snapshot>");
  }

  protected void saveSnapshot(String snapshotId, String value) {
    Checks.notNull(snapshotId, () -> "snapshotId is null");
    Checks.notNull(value, () -> "value is null");
    snapshotStore.saveSnapshot(snapshotId, wrapper.wrap(value));
  }

  protected void saveActual(String snapshotId, String value) {
    Checks.notNull(snapshotId, () -> "snapshotId is null");
    Checks.notNull(value, () -> "value is null");
    snapshotStore.saveActual(snapshotId, wrapper.wrap(value));
  }

  protected void deleteActual(String snapshotId) {
    Checks.notNull(snapshotId, () -> "snapshotId is null");
    snapshotStore.deleteActual(snapshotId);
  }

  /**
   * Declares a {@link Wrapper} for wrapping and unwrapping of the reference value.
   * <P>
   * It is suggested that users create subclasses of {@link Snapshotter} with common wrapped types,
   * instead of using this method directly.
   */
  public Snapshotter wrappedAs(Wrapper wrapper) {
    Checks.notNull(wrapper, () -> "wrapper is null");
    this.wrapper = wrapper;
    return this;
  }

  /**
   * Declares a {@link Function} for normalizing the reference value.
   * <P>
   * The default normalization is line ending normalization.
   */
  public Snapshotter normalizedWith(Function<String, String> normalizer) {
    Checks.notNull(normalizer, () -> "normalizer is null");
    this.normalizer = normalizer;
    return this;
  }

  /**
   * Specifies the {@code actual} value.
   * <p>
   * Note: The value is normalized by the normalizer function, defaults to line ending normalization.
   * @param value the actual value for assertions
   */
  public Snapshot assertThat(String value) {
    Checks.notNull(value, () -> "value is null");
    return new Snapshot(value);
  }

  @Override
  public String toString() {
    return "Snapshotter";
  }

  public class Snapshot {
    private final String value;

    private Snapshot(String value) {
      this.value = Checks.notNull(
        normalizer.apply(Checks.notNull(value, () -> "value is null")),
        () -> "normalizer returned null value"
      );
    }

    /**
     * Asserts that the actual value matches the snapshot using string equality.
     */
    public void matchesSnapshot() {
      matchesSnapshot("");
    }

    /**
     * Asserts that the actual value matches the snapshot using string equality.
     *
     * @param snapshotId the id of the snapshot to use. In case of multiple snapshots per test, this allows to distinguish them.
     */
    public void matchesSnapshot(String snapshotId) {
      matchesSnapshot(snapshotId, (expected, actual) -> {
        if (!Objects.equals(expected, actual)) {
          // manually construct a ConditionNotSatisfiedError, as the native groovy assert doesn't get properly rendered by Intellij
          throw new ConditionNotSatisfiedError(new Condition(Arrays.asList(value, expected), "value == snapshotValue", TextPosition.create(-1, -1), null, null, null));
        }
      });
    }

    /**
     * Allows to specify a custom matcher for the snapshot.
     * <p>
     * This is useful when comparing json or similar, which have less strict equality rules.
     *
     * @param snapshotMatcher a custom matcher for the snapshot, which is called with the snapshot value and the actual value.
     * The matcher must throw an {@link AssertionError} if the values don't match.
     */
    public void matchesSnapshot(BiConsumer<String, String> snapshotMatcher) {
      matchesSnapshot("", snapshotMatcher);
    }

    /**
     * Allows to specify a custom matcher for the snapshot.
     * <p>
     * This is useful when comparing json or similar, which have less strict equality rules.
     *
     * @param snapshotId the id of the snapshot to use. In case of multiple snapshots per test, this allows to distinguish them.
     * @param snapshotMatcher a custom matcher for the snapshot, which is called with the snapshot value and the actual value.
     * The matcher must throw an {@link AssertionError} if the values don't match.
     */
    public void matchesSnapshot(String snapshotId, BiConsumer<String, String> snapshotMatcher) {
      Checks.notNull(snapshotId, () -> "snapshotId is null");
      Checks.notNull(snapshotMatcher, () -> "snapshotMatcher is null");

      String snapshotValue = loadSnapshot(snapshotId);
      try {
        snapshotMatcher.accept(snapshotValue, value);
        if (snapshotStore.isWriteActualContents()) {
          deleteActual(snapshotId);
        }
      } catch (AssertionError e) {
        if (snapshotStore.isUpdateSnapshots()) {
          saveSnapshot(snapshotId, value);
        } else {
          if (snapshotStore.isWriteActualContents()) {
            saveActual(snapshotId, value);
          }
          throw e;
        }
      }
    }

    @Override
    public String toString() {
      return "Snapshot";
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
      this.prefix = Checks.notNull(prefix, () -> "prefix is null");
      this.suffix = Checks.notNull(suffix, () -> "suffix is null");
    }

    public static PrefixSuffixWrapper of(String prefix, String suffix) {
      return new PrefixSuffixWrapper(prefix, suffix);
    }

    public static PrefixSuffixWrapper asciiDocSample(String prefix, String suffix) {
      Checks.notNull(prefix, () -> "prefix is null");
      Checks.notNull(suffix, () -> "suffix is null");
      return new PrefixSuffixWrapper(prefix + "\n/*--------- tag::snapshot[] ---------*/\n", "\n/*--------- end::snapshot[] ---------*/\n" + suffix);
    }

    @Override
    public String wrap(String value) {
      return prefix + value + suffix;
    }

    @Override
    public String unwrap(String value) {
      Checks.notNull(value, () -> "value is null");
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
    private final IterationInfo iterationInfo;
    private final boolean updateSnapshots;
    private final boolean writeActualContents;
    private final String extension;
    private final Charset charset;
    private final Path specPath;

    public Store(IterationInfo iterationInfo, Path rootPath, boolean updateSnapshots, boolean writeActualContents, String extension, Charset charset) {
      this.iterationInfo = Assert.notNull(iterationInfo);
      this.updateSnapshots = Assert.notNull(updateSnapshots);
      this.writeActualContents = writeActualContents;
      this.extension = Assert.notNull(extension);
      this.charset = Assert.notNull(charset);

      Class<?> specClass = iterationInfo.getFeature().getSpec().getReflection();
      specPath = rootPath
        .resolve(specClass.getPackage().getName().replace('.', '/'))
        .resolve(specClass.getSimpleName().replace('$', '/')); // use subdirectories for inner classes
    }

    private static String calculateSafeUniqueName(String extension, IterationInfo iterationInfo, String snapshotId) {
      Checks.checkArgument(snapshotId.length() <= 100,
        () -> String.format("'snapshotId' is too long, only 100 characters are allowed, but was %d: %s", snapshotId.length(), snapshotId));

      FeatureInfo feature = iterationInfo.getFeature();
      String safeName = sanitize(feature.getName());
      String featureId = feature.getFeatureMethod().getReflection().getName().substring("$spock_feature_".length());
      String iterationIndex = feature.isParameterized() ? String.format(Locale.ROOT, "-[%d]", iterationInfo.getIterationIndex()) : "";
      String snapshotIdSuffix = snapshotId.isEmpty() ? "" : "-" + sanitize(snapshotId);

      int uniqueSuffixLength = 1 + featureId.length() + 1 + extension.length() + iterationIndex.length() + snapshotIdSuffix.length();
      if (safeName.length() + uniqueSuffixLength > 250) {
        safeName = safeName.substring(0, 250 - uniqueSuffixLength);
        return String.format(Locale.ROOT, "%s%s-%s%s.%s", safeName, snapshotIdSuffix, featureId, iterationIndex, extension);
      }
      return String.format(Locale.ROOT, "%s%s%s.%s", safeName, snapshotIdSuffix, iterationIndex, extension);
    }

    private static String sanitize(String snapshotId) {
      return Assert.notNull(snapshotId).replaceAll("[^a-zA-Z0-9]", "_");
    }

    public Optional<String> loadSnapshot(String snapshotId) {
      Assert.notNull(snapshotId);
      Path snapshotPath = specPath.resolve(calculateSafeUniqueName(extension, iterationInfo, snapshotId));
      if (Files.isRegularFile(snapshotPath)) {
        try {
          return Optional.of(IoUtil.getText(snapshotPath, charset));
        } catch (IOException e) {
          throw new UncheckedIOException("Failure while trying to load Snapshot: " + snapshotPath, e);
        }
      }
      return Optional.empty();
    }


    public void saveSnapshot(String snapshotId, String value) {
      Assert.notNull(snapshotId);
      Assert.notNull(value);
      try {
        Path snapshotPath = specPath.resolve(calculateSafeUniqueName(extension, iterationInfo, snapshotId));
        Files.createDirectories(specPath);
        IoUtil.writeText(snapshotPath, value, charset);
      } catch (IOException e) {
        throw new UncheckedIOException("Could not create directories", e);
      }
    }

    public void saveActual(String snapshotId, String value) {
      Assert.notNull(snapshotId);
      Assert.notNull(value);
      try {
        Path snapshotPath = specPath.resolve(calculateSafeUniqueName(extension + ".actual", iterationInfo, snapshotId));
        Files.createDirectories(specPath);
        IoUtil.writeText(snapshotPath, value, charset);
        System.err.println("Snapshot actual value has been saved to: " + snapshotPath.toAbsolutePath());
      } catch (IOException e) {
        throw new UncheckedIOException("Could not create directories", e);
      }
    }

    public void deleteActual(String snapshotId) {
      Assert.notNull(snapshotId);
      try {
        Path snapshotPath = specPath.resolve(calculateSafeUniqueName(extension + ".actual", iterationInfo, snapshotId));
        Files.deleteIfExists(snapshotPath);
      } catch (IOException e) {
        throw new UncheckedIOException("Could not delete file", e);
      }
    }

    public boolean isUpdateSnapshots() {
      return updateSnapshots;
    }

    public boolean isWriteActualContents() {
      return writeActualContents;
    }
  }
}
