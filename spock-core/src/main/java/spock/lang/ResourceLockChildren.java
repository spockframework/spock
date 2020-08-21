package spock.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ResourceLockChildrenExtension;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.spockframework.util.Beta;

/**
 * Allows to control access to a shared resource.
 *
 * This is the same as applying {@link ResourceLock} on every child.
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
   * The name of the resource.
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
