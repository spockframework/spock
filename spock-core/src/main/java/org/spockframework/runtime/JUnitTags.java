package org.spockframework.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @JUnitTags} is a container for one or more {@link JUnitTag} declarations.
 *
 * <p>Note, however, that use of the {@code @JUnitTags} container is completely
 * optional since {@code @JUnitTag} is a {@linkplain java.lang.annotation.Repeatable
 * repeatable} annotation.
 *
 * @since 2.2
 * @see JUnitTag
 * @see java.lang.annotation.Repeatable
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JUnitTags {
  /**
   * An array of one or more {@link JUnitTag}s.
   */
  JUnitTag[] value();
}
