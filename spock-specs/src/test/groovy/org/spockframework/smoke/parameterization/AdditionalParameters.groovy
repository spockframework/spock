package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockExecutionException
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target


class AdditionalParameters extends EmbeddedSpecification {
  void setup() {
    runner.addClassImport(ProvideValues.class)
    runner.addClassImport(ProvideNullValues.class)
  }

  def "should be possible to add missing parameters in runtime [no parametrization case]"() {
    when:
        runner.runSpecBody("""
          @ProvideValues(["a", "b"])
          def test(String a, String b){
            expect:
              a == "a"
              b == "b"
          }

""")
    then:
        noExceptionThrown()
  }

  def "should be possible to add missing parameters in runtime [parametrization case]"() {
    when:
        runner.runSpecBody("""
          @ProvideValues(["a", "c"])
          def test(String a, String b, String c){
            expect:
              a == "a"
              b == "b"
              c == "c"
            where:
              b << "b"
          }

""")
    then:
        noExceptionThrown()
  }

  def "should report error if not all parameters was set"() {
    when:
        runner.runSpecBody("""
          @ProvideValues(["b"])
          def test(String a, String b, String c){
            expect:
              true
            where:
              a << "a"
          }

""")
    then:
        thrown(SpockExecutionException.class)
  }

  def "should be possible to set parameters to null"() {
    when:
        runner.runSpecBody("""
          @ProvideNullValues(["b"])
          def test(String a, String b){
            expect:
              a == null
              b == null
            where:
              a << [null]
          }

""")
    then:
        noExceptionThrown()
  }

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtensionAnnotation(ProvideValuesExtension.class)
public @interface ProvideValues {
  String[] value();
}

public class ProvideValuesExtension extends AbstractAnnotationDrivenExtension<ProvideValues> {
  @Override
  void visitFeatureAnnotation(ProvideValues annotation, FeatureInfo feature) {
    feature.addIterationInterceptor(new IMethodInterceptor() {
      @Override
      void intercept(IMethodInvocation invocation) throws Throwable {
        for (String name : annotation.value()) {
          invocation.getIteration().getDataValues().put(name, name);
        }
        invocation.proceed()
      }
    })
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtensionAnnotation(ProvideNullValuesExtension.class)
public @interface ProvideNullValues {
  String[] value();
}

public class ProvideNullValuesExtension extends AbstractAnnotationDrivenExtension<ProvideNullValues> {
  @Override
  void visitFeatureAnnotation(ProvideNullValues annotation, FeatureInfo feature) {
    feature.addIterationInterceptor(new IMethodInterceptor() {
      @Override
      void intercept(IMethodInvocation invocation) throws Throwable {
        for (String name : annotation.value()) {
          invocation.getIteration().getDataValues().put(name, null);
        }
        invocation.proceed()
      }
    })
  }
}
