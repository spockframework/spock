package spock.lang;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.PendingFeatureExtension;

/**
 * Indicates that the feature is not fully implemented yet and should not be reported as error.
 * <p>
 * The use case is to annotate tests that can not yet run but should already be committed.
 * The main difference to {@link Ignore} is that the test are executed, but test failures are ignored.
 * If the test passes without an error, then it will be reported as failure since the {@link PendingFeature}
 * annotation should be removed. This way the tests will become part of the normal tests
 * instead of being ignored forever.
 * </p>
 * <p>
 * Groovy has the {@link groovy.transform.NotYetImplemented} annotation which is similar but behaves a differently.
 * <ul>
 *     <li>it will mark failing tests as passed</li>
 *     <li>if at least one iteration of a data-driven test passes it will be reported as error</li>
 * </ul>
 * {@link PendingFeature}:
 * <ul>
 *     <li>it will mark failing tests as skipped</li>
 *     <li>if at least one iteration of a data-driven test fails it will be reported as skipped</li>
 *     <li>if every iteration of a data-driven test passes it will be reported as error</li>
 * </ul>
 *</p>
 * @author Leonard Brünings
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ExtensionAnnotation(PendingFeatureExtension.class)
public @interface PendingFeature {
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
