package spock.lang;

import groovy.lang.Closure;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.PendingFeatureIfExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated spec or feature as {@link PendingFeature} if the given condition holds.
 * Otherwise it will be treated like a normal feature.
 * <p />
 * <p />
 * The configured closure is called with a delegate of type
 * {@link org.spockframework.runtime.extension.builtin.PreconditionContext}
 * which provides access to system properties, environment variables, the type
 * of operating system and JVM.
 * <p />
 * <p />
 * Relates annotations (extensions): {@link Requires}, {@link IgnoreIf}
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ExtensionAnnotation(PendingFeatureIfExtension.class)
public @interface PendingFeatureIf {

  /**
   * If this condition holds true, the feature will be considered "Pending"
   * and will behave the same way as if it were annotated as a {@link PendingFeature}
   *
   * If the condition is false, it will not be treated as pending
   *
   * @return Closure to be evaluated
   */
  Class<? extends Closure> value();

  /**
   * Configures which types of Exceptions are expected in the pending feature.
   *
   * Subclasses are included if their parent class is listed.
   *
   * @return array of Exception classes to ignore.
   */
  Class<? extends Throwable>[] exceptions() default {Exception.class};

  /**
   * The reason why this feature is pending
   *
   * @return reason why this feature is pending
   */
  String reason() default "";
}
