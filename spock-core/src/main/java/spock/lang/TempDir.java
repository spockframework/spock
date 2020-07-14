package spock.lang;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import org.spockframework.runtime.extension.builtin.TempDirExtension;
import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generate temp directory for test, and clean them after test.
 *
 * <p>`@TempDir` can be applied to annotate a member field of type {@link File} or {@link Path}
 * in a spec class. If annotated field is shared, the temp directory will be shared in this spec, otherwise every
 * feature will have its' own temp directory.
 *
 *
 * @author: dqyuan
 * @date: 2020/07/12
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ExtensionAnnotation(TempDirExtension.class)
public @interface TempDir {
}
