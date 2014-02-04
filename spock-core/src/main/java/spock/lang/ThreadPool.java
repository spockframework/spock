package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ThreadPoolExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify the number of concurrent threads that should
 * be used in the execution of a feature or fixture method. The default is 1
 *
 * @author Bayo Erinle
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ExtensionAnnotation(ThreadPoolExtension.class)
public @interface ThreadPool {

    /**
     * The number of concurrent threads used to execute a feature or fixture method.
     *
     * @return the number of concurrent threads used to execute a feature or fixture method
     */

    int value() default 1;

}
