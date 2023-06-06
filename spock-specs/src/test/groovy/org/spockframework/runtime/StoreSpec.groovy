package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.MethodKind
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.Assert
import org.spockframework.util.InternalSpockError

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class StoreSpec extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(LogStoreUsage)
    runner.addClassImport(FailingStoreUsage)
    runner.addClassMemberImport(MethodKind)
  }

  def "store is created and cleaned for each level"() {
    when:
    println "------------TEST START----------------"
    runner.runWithImports('''
    @LogStoreUsage
    class ASpec extends Specification {
      def "a feature"() {
        expect: true
      }
    }
''')
    println "------------TEST END------------------"

    then:
    noExceptionThrown()
  }

  def "store cleanup fails"() {
    when:
    println "------------TEST START----------------"
    runner.runWithImports("""
    @FailingStoreUsage($methodKind)
    class ASpec extends Specification {
      def "a feature"() {
        expect: true
      }
    }
""")
    println "------------TEST END------------------"

    then:
    InternalSpockError e = thrown()
    e.message.startsWith("Failing at $methodKind - ")

    where:
    methodKind << (MethodKind.values() - [MethodKind.DATA_PROCESSOR, MethodKind.DATA_PROVIDER])
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(StoreExtension)
@interface LogStoreUsage {
}

class StoreExtension implements IAnnotationDrivenExtension<LogStoreUsage> {
  @Override
  void visitSpecAnnotation(LogStoreUsage annotation, SpecInfo spec) {
    def specInfo = spec.bottomSpec
    specInfo.addSharedInitializerInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.sharedInitializerMethod?.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.addSetupSpecInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.setupSpecMethods*.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.allFeatures*.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.addInitializerInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.initializerMethod?.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.allFeatures*.addIterationInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.addSetupInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.setupMethods*.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.allFeatures*.featureMethod*.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.addCleanupInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.cleanupMethods*.addInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.addCleanupSpecInterceptor LoggingStoreInterceptor.INSTANCE
    specInfo.cleanupSpecMethods*.addInterceptor LoggingStoreInterceptor.INSTANCE
  }
}

class LoggingStoreInterceptor implements IMethodInterceptor {

  static final IStore.Namespace NAMESPACE = IStore.Namespace.create(LoggingStoreInterceptor.class)
  static final LoggingStoreInterceptor INSTANCE = new LoggingStoreInterceptor()

  @Override
  void intercept(IMethodInvocation invocation) throws Throwable {
    def store = invocation.getStore(NAMESPACE)
    def prev = store.get("message", LoggingValue)
    def newValue = new LoggingValue("Stored at $invocation.method.kind - $invocation.method.name")
    def replaced = store.put("message", newValue)
    println "before $invocation.method.kind - $invocation.method.name"
    println("\tprev:     ${prev?.value}")
    println("\treplaced: ${replaced?.value}")
    println("\tput:      ${newValue.value}")
    invocation.proceed()
    println "after $invocation.method.kind - $invocation.method.name"
  }
}

class LoggingValue implements AutoCloseable {

  final String value

  LoggingValue(String value) {
    this.value = value
  }

  @Override
  void close() throws Exception {
    println "# closing ${this.value}"
  }
}


@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(FailingCleanupStoreExtension)
@interface FailingStoreUsage {
  MethodKind[] value() default []
}


class FailingCleanupStoreExtension implements IAnnotationDrivenExtension<FailingStoreUsage> {
  @Override
  void visitSpecAnnotation(FailingStoreUsage annotation, SpecInfo spec) {
    def specInfo = spec.bottomSpec
    def interceptor = new FailingCleanupStoreInterceptor(annotation)
    specInfo.addSharedInitializerInterceptor interceptor
    specInfo.sharedInitializerMethod?.addInterceptor interceptor
    specInfo.addInterceptor interceptor
    specInfo.addSetupSpecInterceptor interceptor
    specInfo.setupSpecMethods*.addInterceptor interceptor
    specInfo.allFeatures*.addInterceptor interceptor
    specInfo.addInitializerInterceptor interceptor
    specInfo.initializerMethod?.addInterceptor interceptor
    specInfo.allFeatures*.addIterationInterceptor interceptor
    specInfo.addSetupInterceptor interceptor
    specInfo.setupMethods*.addInterceptor interceptor
    specInfo.allFeatures*.featureMethod*.addInterceptor interceptor
    specInfo.addCleanupInterceptor interceptor
    specInfo.cleanupMethods*.addInterceptor interceptor
    specInfo.addCleanupSpecInterceptor interceptor
    specInfo.cleanupSpecMethods*.addInterceptor interceptor
  }
}

class FailingCleanupStoreInterceptor implements IMethodInterceptor {

  static final IStore.Namespace NAMESPACE = IStore.Namespace.create(FailingCleanupStoreInterceptor.class)

  final FailingStoreUsage annotation

  FailingCleanupStoreInterceptor(FailingStoreUsage annotation) {
    this.annotation = annotation
  }

  @Override
  void intercept(IMethodInvocation invocation) throws Throwable {
    def store = invocation.getStore(NAMESPACE)
    store.put(invocation.method.kind,
      new FailingValue(
        "Failing at $invocation.method.kind - $invocation.method.name",
        annotation.value().contains(invocation.method.kind))
    )
    invocation.proceed()
  }
}

class FailingValue implements AutoCloseable {

  final String value
  final boolean failAtCleanup

  FailingValue(String value, boolean failAtCleanup) {
    this.failAtCleanup = failAtCleanup
    this.value = value
  }

  @Override
  void close() throws Exception {
    Assert.that(!failAtCleanup, value)
  }
}
