package common.extension.spock.lang

import common.extension.spock.runtime.TagsExtension
import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * User: gcurrey
 * Date: 12/12/13
 * Time: 8:46 AM
 *
 * This annotation allows you to tag a feature test or specification.  Tagging allows you to exeute a subset of all
 * tests at runtime.
 *
 * eg:
 *
 * @Tags(["UAT","STAGE"])
 *
 * And then at runtime you must pass a system property as below.  If you supply a tag and the test is annotated with the same
 * tag, the test will execute, otherwise, it will be ignored.
 *
 * ... -Dtags=UAT
 *
 * or
 *
 * ... -Dtags=PROD
 *
 * If a test is not tagged and the -Dtags property is set, the test will not be run.
 * If a test is tagged and the -Dtags property is not set, the test will be run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtensionAnnotation(TagsExtension.class)
public @interface Tags {
    String[] value() default new String[]{};
}
