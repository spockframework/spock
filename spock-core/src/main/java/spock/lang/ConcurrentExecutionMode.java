package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.ConcurrentExecutionModeExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets concurrent execution mode.
 * For parallel execution Spock use RunnerScheduler passed by JUnit (therefore features and iterations will be executed in parallel only if parallel scheduler passed to runner).
 * <p/>
 * If it is set on feature level if will describe can or not iterations be executed concurrently (should them be scheduled or should executed concurrently).
 * It affects only @Unroll'ed features.
 * <p/>
 * On class level this annotations will describe can or not features and unrolled iterations be executed concurrently.
 * <p/>
 * If it is set on class level it will affect all features unless feature has it's own annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(ConcurrentExecutionModeExtension.class)
public @interface ConcurrentExecutionMode {
  Mode value();

  enum Mode{
    FORCE_SEQUENTIAL,
    USE_JUNIT_SCHEDULER
  }
}
