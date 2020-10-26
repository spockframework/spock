package spock.config

import org.spockframework.builder.DelegatingScript
import org.spockframework.runtime.ConfigurationBuilder
import org.spockframework.runtime.GroovyRuntimeUtil

trait ConfigSupport {

  ConfigurationBuilder builder = new ConfigurationBuilder()

  DelegatingScript closureToScript(final Closure<?> closure) {
    return new ClosureScript(closure)
  }
}

class ClosureScript extends DelegatingScript {
  final Closure<?> closure

  ClosureScript(Closure<?> closure) {
    this.closure = closure
  }

  @Override
  Object run() {
    GroovyRuntimeUtil.invokeClosure(closure)
    return null
  }

  @Override
  void $setDelegate(Object delegate) {
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.delegate = delegate
  }
}

