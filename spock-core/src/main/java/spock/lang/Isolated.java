package spock.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.IsolatedExtension;
import org.spockframework.util.Beta;

/**
 * Allows the isolated execution of a Specification.
 *
 * No other specification will execute at the same time.
 * This will also cause all features to run on the same thread
 * as the Specification.
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@ExtensionAnnotation(IsolatedExtension.class)
public @interface Isolated {
}
