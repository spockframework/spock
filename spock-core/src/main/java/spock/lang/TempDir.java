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
 * Generate temp directory for test, and clean them after test.
 *
 * <p>{@code @TempDir} can be applied to annotate a member field of type {@link File} or {@link Path}
 * in a spec class. If annotated field is shared, the temp directory will be shared in this spec, otherwise every
 * iteration will have its own temp directory.
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
  /**
   * The parent directory for the temporary folder, default is system temp directory
   * @return the parent directory for the temporary folder
   */
  String baseDir() default "";

  class DefaultClosure extends Closure {
    public DefaultClosure(Object owner, Object thisObject) {
      super(owner, thisObject);
    }

    public Object doCall(Object args) {
      return false;
    }
  }

  /**
   *
   * Closure to evaluate whether reserve temp directory or not after test, default is not
   *
   * The configured closure is called with a delegate of type {@link PreconditionContext}
   * which provides access to system properties, environment variables, the type
   * of operating system and JVM.
   *
   * @return Closure to evaluate whether reserve temp directory or not after test, default is not
   */
  Class<? extends Closure> reserveAfterTest() default DefaultClosure.class;
}
