package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.MethodKind
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.spockframework.util.Assert
import org.spockframework.util.InternalSpockError
import spock.lang.Execution
import spock.lang.Shared

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class StoreSpec extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(LogStoreUsage)
    runner.addClassImport(FailingStoreUsage)
    runner.addClassMemberImport(MethodKind)
  }

  def "store is created and cleaned for each level"() {
    given:
    LoggingStoreInterceptor.HACKY_SHARED_LIST_OF_ACTIONS.clear()

    when:
    runner.runWithImports('''
    @LogStoreUsage
    class ASpec extends Specification {
      def "a feature"() {
        expect: true
      }
    }
''')

    then:
    LoggingStoreInterceptor.HACKY_SHARED_LIST_OF_ACTIONS == [
      "before SHARED_INITIALIZER - null",
      "	prev:     null",
      "	replaced: null",
      "	put:      Stored at SHARED_INITIALIZER - null",
      "after SHARED_INITIALIZER - null",
      "before SPEC_EXECUTION - null",
      "	prev:     Stored at SHARED_INITIALIZER - null",
      "	replaced: Stored at SHARED_INITIALIZER - null",
      "	put:      Stored at SPEC_EXECUTION - null",
      "before SETUP_SPEC - null",
      "	prev:     Stored at SPEC_EXECUTION - null",
      "	replaced: Stored at SPEC_EXECUTION - null",
      "	put:      Stored at SETUP_SPEC - null",
      "after SETUP_SPEC - null",
      "before INITIALIZER - null",
      "	prev:     Stored at SETUP_SPEC - null",
      "	replaced: null",
      "	put:      Stored at INITIALIZER - null",
      "after INITIALIZER - null",
      "before FEATURE_EXECUTION - null",
      "	prev:     Stored at INITIALIZER - null",
      "	replaced: Stored at INITIALIZER - null",
      "	put:      Stored at FEATURE_EXECUTION - null",
      "before ITERATION_EXECUTION - null",
      "	prev:     Stored at FEATURE_EXECUTION - null",
      "	replaced: Stored at FEATURE_EXECUTION - null",
      "	put:      Stored at ITERATION_EXECUTION - null",
      "before SETUP - null",
      "	prev:     Stored at ITERATION_EXECUTION - null",
      "	replaced: Stored at ITERATION_EXECUTION - null",
      "	put:      Stored at SETUP - null",
      "after SETUP - null",
      "before FEATURE - a feature",
      "	prev:     Stored at SETUP - null",
      "	replaced: Stored at SETUP - null",
      "	put:      Stored at FEATURE - a feature",
      "after FEATURE - a feature",
      "before CLEANUP - null",
      "	prev:     Stored at FEATURE - a feature",
      "	replaced: Stored at FEATURE - a feature",
      "	put:      Stored at CLEANUP - null",
      "after CLEANUP - null",
      "# closing Stored at CLEANUP - null",
      "# closing Stored at FEATURE - a feature",
      "# closing Stored at SETUP - null",
      "# closing Stored at ITERATION_EXECUTION - null",
      "# closing Stored at FEATURE_EXECUTION - null",
      "# closing Stored at INITIALIZER - null",
      "after ITERATION_EXECUTION - null",
      "after FEATURE_EXECUTION - null",
      "before CLEANUP_SPEC - null",
      "	prev:     Stored at SETUP_SPEC - null",
      "	replaced: Stored at SETUP_SPEC - null",
      "	put:      Stored at CLEANUP_SPEC - null",
      "after CLEANUP_SPEC - null",
      "# closing Stored at CLEANUP_SPEC - null",
      "# closing Stored at SETUP_SPEC - null",
      "# closing Stored at SPEC_EXECUTION - null",
      "# closing Stored at SHARED_INITIALIZER - null",
      "after SPEC_EXECUTION - null"
    ]
  }

  @Shared
  def supportedMethodKinds = ((MethodKind.values() as List) - [MethodKind.DATA_PROCESSOR, MethodKind.DATA_PROVIDER])

  @Execution(ExecutionMode.SAME_THREAD)
  def "store cleanup fails"() {
    given:
    FailingValue.HACKY_SHARED_LIST_OF_CLEANUPS.clear()

    when:
    runner.runWithImports("""
    @FailingStoreUsage([${methodKind.join(', ')}])
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
    methodKind << supportedMethodKinds.subsequences()
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

  static List<MethodKind> HACKY_SHARED_LIST_OF_ACTIONS = []

  @Override
  void intercept(IMethodInvocation invocation) throws Throwable {
    def store = invocation.getStore(NAMESPACE)
    String message = "Stored at $invocation.method.kind - $invocation.method.name"
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
