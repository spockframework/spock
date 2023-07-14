package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.MethodKind
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter
import org.spockframework.util.Assert
import org.spockframework.util.InternalSpockError
import spock.lang.Execution
import spock.lang.ResourceLock
import spock.lang.Shared

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class StoreSpec extends EmbeddedSpecification {

  @Snapshot
  Snapshotter snapshotter

  def setup() {
    runner.addClassImport(LogStoreUsage)
    runner.addClassImport(FailingStoreUsage)
    runner.addClassMemberImport(MethodKind)
  }

  @ResourceLock("org.spockframework.runtime.LogStoreUsage")
  def "store is created and cleaned for each level"() {
    given:
    LoggingStoreInterceptor.reset()

    when:
    runner.runWithImports('''
    @LogStoreUsage
    class ASpec extends Specification {
      def "a feature"() {
        expect: true
      }

      def "data driven feature"() {
        expect: true
        where:
        i << [1, 2]
      }
    }
''')

    then:
    snapshotter.assertThat(LoggingStoreInterceptor.HACKY_SHARED_LIST_OF_ACTIONS.join("\n")).matchesSnapshot()
  }

  @Shared
  def supportedMethodKinds = ((MethodKind.values() as List) - [MethodKind.DATA_PROCESSOR, MethodKind.DATA_PROVIDER])

  @ResourceLock("org.spockframework.runtime.FailingStoreUsage")
  def "store cleanup fails"() {
    given:
    FailingValue.HACKY_SHARED_LIST_OF_CLEANUPS.clear()

    when:
    runner.runWithImports("""
    @FailingStoreUsage([${methodKinds.join(', ')}])
    class ASpec extends Specification {
      def "a feature"() {
        expect: true
      }
    }
""")

    then:
    InternalSpockError e = thrown()
    e.message.startsWith("Failing at ")
    FailingValue.HACKY_SHARED_LIST_OF_CLEANUPS ==~ supportedMethodKinds

    where:
    methodKinds << supportedMethodKinds.subsequences()
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(LogStoreExtension)
@interface LogStoreUsage {
}

class LogStoreExtension implements IAnnotationDrivenExtension<LogStoreUsage> {
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

  static List<String> HACKY_SHARED_LIST_OF_ACTIONS = []
  static int counter

  static void reset() {
    HACKY_SHARED_LIST_OF_ACTIONS.clear()
    counter = 0
  }

  @Override
  void intercept(IMethodInvocation invocation) throws Throwable {
    def store = invocation.getStore(NAMESPACE)
    String message = "Stored at $invocation.method.kind - $invocation.method.name [${counter++}]"
    def prev = store.get("message", String)
    def replaced = store.put("message", message)

    store.put(invocation.method.kind, new LoggingValue(message))

    HACKY_SHARED_LIST_OF_ACTIONS << "before $invocation.method.kind - $invocation.method.name".toString()
    HACKY_SHARED_LIST_OF_ACTIONS << "\tprev:     ${prev}".toString()
    HACKY_SHARED_LIST_OF_ACTIONS << "\treplaced: ${replaced}".toString()
    HACKY_SHARED_LIST_OF_ACTIONS << "\tput:      ${message}".toString()
    invocation.proceed()
    HACKY_SHARED_LIST_OF_ACTIONS << "after $invocation.method.kind - $invocation.method.name".toString()
  }
}

class LoggingValue implements AutoCloseable {

  final String value

  LoggingValue(String value) {
    this.value = value
  }

  @Override
  void close() throws Exception {
    LoggingStoreInterceptor.HACKY_SHARED_LIST_OF_ACTIONS << "# closing ${this.value}".toString()
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
        invocation.method.kind,
        "Failing at $invocation.method.kind - $invocation.method.name",
        annotation.value().contains(invocation.method.kind))
    )
    invocation.proceed()
  }
}

class FailingValue implements AutoCloseable {

  static List<MethodKind> HACKY_SHARED_LIST_OF_CLEANUPS = []

  final String value
  final boolean failAtCleanup
  private final MethodKind methodKind

  FailingValue(MethodKind methodKind, String value, boolean failAtCleanup) {
    this.methodKind = methodKind
    this.failAtCleanup = failAtCleanup
    this.value = value
  }

  @Override
  void close() throws Exception {
    HACKY_SHARED_LIST_OF_CLEANUPS << methodKind
    Assert.that(!failAtCleanup, value)
  }
}
