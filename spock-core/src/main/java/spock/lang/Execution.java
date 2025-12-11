package spock.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ExecutionExtension;
import org.spockframework.runtime.model.parallel.ExecutionMode;

/**
 * Allows to set the execution mode.
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(ExecutionExtension.class)
public @interface Execution {

  /**
   * The required/preferred execution mode.
   *
   * @see ExecutionMode
   */
  ExecutionMode value();

  /**
   * The reason of using a non-default execution mode.
   *
   * @since 2.4
   */
  String reason() default "";

}
