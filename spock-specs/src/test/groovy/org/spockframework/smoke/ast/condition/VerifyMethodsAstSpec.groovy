package org.spockframework.smoke.ast.condition

import spock.lang.Verify

import java.lang.annotation.Annotation

class VerifyMethodsAstSpec extends BaseVerifyMethodsAstSpec {

  @Override
  Class<? extends Annotation> getAnnotation() {
    Verify
  }

}
