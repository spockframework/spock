package org.spockframework.tempdir;

import groovy.lang.Closure;
import org.spockframework.runtime.extension.builtin.PreconditionContext;
import org.spockframework.util.Beta;
import spock.config.ConfigurationObject;

import java.nio.file.Path;

/**
 *
 * Configuration settings for the TempDir extension.
 *
 * <p>Example:
 * <pre>
 * tempdir {
 *   baseDir false // default null, which means {@code System.getProperty("java.io.tmpdir")}
 *   keep { os.windows } // default { false }, which is called with a delegate of type {@link PreconditionContext}
 * }
 * </pre>
 * @author dqyuan
 * @since 2.0
 * @see PreconditionContext
 */
@Beta
@ConfigurationObject("tempdir")
public class TempDirConfiguration {
  /**
   * DefaultClosure always returns null, which means follow global configuration.
   * And global configuration is false by default
   */
  public static class DefaultClosure extends Closure {
    public DefaultClosure(Object owner, Object thisObject) {
      super(owner, thisObject);
    }

    public Object doCall(Object args) {
      return null;
    }
  }

  /**
   * The parent directory for the temporary folder, default is system temp directory.
   */
  public Path baseDir = null;

  /**
   *
   * Closure to evaluate whether to keep the temp directory or not after test, default is not.
   *
   * The configured closure is called with a delegate of type {@link PreconditionContext}
   * which provides access to system properties, environment variables, the type
   * of operating system and JVM.
   *
   * */
  public Class<? extends Closure> keep = DefaultClosure.class;
}
