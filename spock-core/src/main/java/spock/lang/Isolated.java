package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.IsolatedExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.*;

/**
 * Allows the isolated execution of a Specification.
 *
 * No other specification will execute at the same time.
 * This will also cause all features to run on the same thread
 * as the Specification.
 *
 * If you need more fine grained control take a look at
 * {@link ResourceLock}
 *
 * @see Execution
 * @see ResourceLock
 *
 * @since 2.0
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@ExtensionAnnotation(IsolatedExtension.class)
public @interface Isolated {
  /**
   * The reason for isolating this specification.
   * <p>
   * Sometimes it is not readily apparent why a Spec was isolated, so you can add a reason here.
   */
  String value() default "";
}
