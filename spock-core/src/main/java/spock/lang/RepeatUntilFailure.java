package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.RepeatUntilFailureExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repeats a feature until it fails or the maximum number of repetitions is reached.
 * <p>
 * For parameterized features, all parameter sets are repeated.
 * <p>
 * This extension is only intended to aid in manual testing and debugging.
 * @author Leonard Brünings
 * @since 2.3
 */
@Beta
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(RepeatUntilFailureExtension.class)
public @interface RepeatUntilFailure {
  /**
   * The maximum number of repetitions.
   */
  int maxAttempts() default Integer.MAX_VALUE;

  /**
   * Behaves like {@link IgnoreRest} is applied, skipping all other features.
   */
  boolean ignoreRest() default true;
}
