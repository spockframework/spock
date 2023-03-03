package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ExecutionExtension;
import org.spockframework.runtime.model.parallel.ExecutionMode;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Allows to set the execution mode.
 *
 * @since 2.0
 */
@Beta
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
