package org.spockframework.smoke.ast.condition

import spock.lang.VerifyAll

import java.lang.annotation.Annotation

class VerifyAllMethodsAstSpec extends BaseVerifyMethodsAstSpec {

  @Override
  Class<? extends Annotation> getAnnotation() {
    VerifyAll
  }

}
