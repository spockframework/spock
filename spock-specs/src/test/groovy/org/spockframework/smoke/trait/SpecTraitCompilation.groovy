package org.spockframework.smoke.trait

import org.spockframework.EmbeddedSpecification

class SpecTraitCompilation extends EmbeddedSpecification {
  def compile() {
    setup:
    runner.runWithImports(
"""
trait Foo implements SpecificationTrait {
  def setup() {}
}

class Bar extends Specification implements Foo {
  def bar() { expect: true }
}
""")
  }
}
