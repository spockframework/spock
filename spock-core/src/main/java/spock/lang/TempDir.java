package spock.lang;

import groovy.lang.Closure;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.TempDirExtension;
import org.spockframework.util.Beta;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

import org.spockframework.runtime.extension.builtin.PreconditionContext;

/**
 * Generate a temp directory for test, and delete it after test.
 *
 * <p>{@code @TempDir} can be applied to annotate a member field of type {@link File}, {@link Path} or untyped like {@code def}
 * in a spec class (untyped field will be injected with {@code Path}). If the annotated field is shared,
 * the temp directory will be shared in this spec, otherwise every iteration will have its own temp directory.
 *
 *
 * @author dqyuan
 * @since 2.0
 * @see PreconditionContext
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(TempDirExtension.class)
public @interface TempDir {
}
