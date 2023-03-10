package org.spockframework.docs.extension

import org.spockframework.runtime.SpockExecutionException
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.ParameterResolver
import org.spockframework.runtime.model.ParameterInfo
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class ParameterInjectionSpec extends Specification {

  def setupSpec(@ParameterIndex int param1) {
    assert param1 == 0
  }

  // tag::example[]
  def setup(@ParameterIndex int param1) {
    assert param1 == 0
  }

  def "test"(@ParameterIndex int param1, @ParameterIndex Integer param2, @ParameterIndex int param3) {
    expect:
    param1 == 0
    param2 == 1
    param3 == 2
  }
  // end::example[]

  def cleanup(@ParameterIndex int param1) {
    assert param1 == 0
  }

  def cleanupSpec(@ParameterIndex int param1) {
    assert param1 == 0
  }
}


// tag::extension[]
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@ExtensionAnnotation(ParameterIndexExtension)
@interface ParameterIndex {}

class ParameterIndexExtension implements IAnnotationDrivenExtension<ParameterIndex> {
  @Override
  void visitParameterAnnotation(ParameterIndex annotation, ParameterInfo parameter) {
    Class<?> type = parameter.reflection.type
    if (!(type in [int, Integer])) {
      throw new SpockExecutionException("Parameter must be a int/Integer but was ${type}")
    }
    parameter.parent.addInterceptor(       // <1>
      new ParameterResolver.Interceptor(   // <2>
        parameter,                         // <3>
        { parameter.index }                // <4>
      ))
  }
}
// end::extension[]
