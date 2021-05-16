package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ResourceLockExtension;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Allows to control access to a shared resource.
 *
 * If applied on class-level, the lock will be on the class,
 * covering shared fields and {@code setupSpec}/{@code cleanupSpec}.
 * This will also cause all features to run on the same thread
 * as the Specification.
 *
 * @see Isolated
 * @see Execution
 *
 * @since 2.0
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(ResourceLockExtension.class)
@Repeatable(ResourceLock.Container.class)
public @interface ResourceLock {
  /**
   * The key identifying the resource.
   *
   * @see org.spockframework.runtime.model.parallel.Resources for a list of standard resources
   * @return the key
   */
  String value();

  /**
   * Controls the access mode of the resource.
   * @return mode to use
   */
  ResourceAccessMode mode() default ResourceAccessMode.READ_WRITE;

  @Beta
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {
    ResourceLock[] value();
  }
}
