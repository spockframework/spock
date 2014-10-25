package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.AssertionModeExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(AssertionModeExtension.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AssertionMode {
  AssertionType value();
}
