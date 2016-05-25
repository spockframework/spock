package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.FailsWithExtension;
import org.spockframework.runtime.extension.builtin.RetryExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reruns failed iterations, reports only last attempt.
 * If systemProperty is specified and <code>Integer.getInteger(Retry.systemProperty()) != null</code> value from system property will be used.
 * Total attempts count will be not more than <code>1 + retryCount</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtensionAnnotation(RetryExtension.class)
public @interface Retry {
  String DO_NOT_USE_SYSTEM_PROPERTY = "DO_NOT_USE_SYSTEM_PROPERTY";

  int value() default 1;

  String systemProperty() default DO_NOT_USE_SYSTEM_PROPERTY;
}
