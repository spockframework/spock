package spock.lang;

import groovy.lang.Closure;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.PendingFeatureIfExtension;
import org.spockframework.runtime.extension.builtin.PreconditionContext;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Marks the annotated feature or selected iterations as {@link PendingFeature} if the
 * given condition holds. Otherwise it will be treated like a normal feature or iteration.
 *
 * <p>The configured closure is called with a delegate of type {@link PreconditionContext}
 * which provides access to system properties, environment variables, the type
 * of operating system and JVM.
 *
 * <p>If applied to a data driven feature, the closure can also access the data variables.
 * If the closure does not reference any actual data variables, the whole feature is deemed pending
 * and only if all iterations become successful will be marked as failing. But if the closure actually
 * does reference valid data variables, the individual iterations where the condition holds are
 * deemed pending and each will individually fail as soon as it would be successful without this annotation.
 *
 * @see PreconditionContext
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ExtensionAnnotation(PendingFeatureIfExtension.class)
@Repeatable(PendingFeatureIf.Container.class)
public @interface PendingFeatureIf {

  /**
   * If this condition holds true, the feature will be considered "Pending"
   * and will behave the same way as if it were annotated as a {@link PendingFeature}
   *
   * <p>If the condition is false, it will not be treated as pending
   *
   * @return Closure to be evaluated
   */
  Class<? extends Closure> value();

  /**
   * Configures which types of Exceptions are expected in the pending feature.
   *
   * <p>Subclasses are included if their parent class is listed.
   *
   * @return array of Exception classes to ignore.
   */
  Class<? extends Throwable>[] exceptions() default {Exception.class};

  /**
   * The reason why this feature is pending
   *
   * @return the string to use for the skip message
   */
  String reason() default "";

  /**
   * @since 2.0
   */
  @Beta
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  @interface Container {
    PendingFeatureIf[] value();
  }
}
