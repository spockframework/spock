package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ResourceLockChildrenExtension;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Allows to control access to a shared resource.
 *
 * This is the same as applying {@link ResourceLock} on every child.
 *
 * @see ResourceLock
 * @see Isolated
 *
 * @since 2.0
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ExtensionAnnotation(ResourceLockChildrenExtension.class)
@Repeatable(ResourceLockChildren.Container.class)
public @interface ResourceLockChildren {
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
  @Target({ElementType.TYPE})
  @interface Container {
    ResourceLockChildren[] value();
  }
}
