package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.TempDirExtension;
import org.spockframework.util.Beta;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

/**
 * Generate a temp directory for test, and delete it after test.
 *
 * <p>
 * {@code @TempDir} can be used to annotate a member field of type {@link File}, {@link Path},
 * or untyped like {@code def}/{@link Object} in a spec class (untyped field will be injected with {@code Path}).
 * <p>
 * Alternatively, you can use it with any class that has a public constructor with a single {@link File} or {@link Path}
 * parameter, like {@link spock.util.io.FsFixture} this way you can use your own utility classes for file manipulation.
 * <p>
 * If the annotated field is shared, the temp directory will be shared in this spec,
 * otherwise every iteration will have its own temp directory.
 * <p>
 * Example:
 * <pre><code>
 * &#64;TempDir
 * File testFile // will inject a File
 *
 * &#64;TempDir
 * Path testPath // will inject a Path
 *
 * &#64;TempDir
 * def testPath // will inject a Path
 *
 * &#64;TempDir
 * FsFixture fsFixture // will inject an instance of FsFixture with the temp path injected via constructor
 * </code></pre>
 *
 * @author dqyuan
 * @since 2.0
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(TempDirExtension.class)
public @interface TempDir {
}
