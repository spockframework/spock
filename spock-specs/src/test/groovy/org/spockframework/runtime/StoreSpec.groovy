package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.MethodKind
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter
import org.spockframework.util.Assert
import org.spockframework.util.InternalSpockError
import spock.config.ConfigurationObject
import spock.lang.Shared

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.concurrent.atomic.AtomicReference

class StoreSpec extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(LogStoreUsage)
    runner.addClassImport(FailingStoreUsage)
    runner.addClassMemberImport(MethodKind)
  }

  def "store is created and cleaned for each level"(@Snapshot Snapshotter snapshotter) {
    given:
    AtomicReference sharedList = new AtomicReference([])
    runner.configClasses << LoggingStoreConfig
    runner.configurationScript {
      loggingStore {
        actionRecorderList sharedList
      }
    }

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
    snapshotter.assertThat(sharedList.get().join("\n")).matchesSnapshot()
  }

  @Shared
  def supportedMethodKinds = ((MethodKind.values() as List) - [MethodKind.DATA_PROCESSOR, MethodKind.DATA_PROVIDER])

  def "properly handles failures during store cleanup"() {
    given:
    AtomicReference sharedList = new AtomicReference([])
    runner.configClasses << FailingCleanupStoreConfig
    runner.configurationScript {
      failingCleanup {
        cleanupRecorderList sharedList
      }
    }

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
    sharedList.get() ==~ supportedMethodKinds

    where:
    methodKinds << supportedMethodKinds.subsequences()
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(LogStoreExtension)
@interface LogStoreUsage {
}

class LogStoreExtension implements IAnnotationDrivenExtension<LogStoreUsage> {
  final LoggingStoreConfig config

  LogStoreExtension (LoggingStoreConfig config) {
    this.config = config
  }

  @Override
  void visitSpecAnnotation(LogStoreUsage annotation, SpecInfo spec) {
    def specInfo = spec.bottomSpec
    def interceptor = new LoggingStoreInterceptor(config.actionRecorderList.get())
    specInfo.addSharedInitializerInterceptor interceptor
    specInfo.allSharedInitializerMethods*.addInterceptor interceptor
    specInfo.addInterceptor interceptor
    specInfo.addSetupSpecInterceptor interceptor
    specInfo.addInitializerInterceptor interceptor
    specInfo.addSetupInterceptor interceptor
    specInfo.allFixtureMethods*.addInterceptor  interceptor
    specInfo.allFeatures*.addInterceptor interceptor
    specInfo.allFeatures*.addIterationInterceptor interceptor
    specInfo.allFeatures*.featureMethod*.addInterceptor interceptor
    specInfo.addCleanupInterceptor interceptor
    specInfo.addCleanupSpecInterceptor interceptor
  }
}

class LoggingStoreInterceptor implements IMethodInterceptor {

  static final IStore.Namespace NAMESPACE = IStore.Namespace.create(LoggingStoreInterceptor.class)

  final List<String> actionList
  int counter

  LoggingStoreInterceptor(List<String> actionList) {
    this.actionList = actionList
  }

  @Override
  void intercept(IMethodInvocation invocation) throws Throwable {
    def store = invocation.getStore(NAMESPACE)
    String message = "Stored at $invocation.method.kind - $invocation.method.name [${counter++}]"
    def prev = store.get("message", String)
    def replaced = store.put("message", message)

    store.put(invocation.method.kind, new LoggingValue(actionList, message))

    actionList << "before $invocation.method.kind - $invocation.method.name".toString()
    actionList << "\tprev:     ${prev}".toString()
    actionList << "\treplaced: ${replaced}".toString()
    actionList << "\tput:      ${message}".toString()
    invocation.proceed()
    actionList << "after $invocation.method.kind - $invocation.method.name".toString()
  }
}

class LoggingValue implements AutoCloseable {

  final String value
  private final List<String> actionList

  LoggingValue(List<String> actionList, String value) {
    this.actionList = actionList
    this.value = value
  }

  @Override
  void close() throws Exception {
    actionList << "# closing ${this.value}".toString()
  }
}

@ConfigurationObject("loggingStore")
class LoggingStoreConfig {
  AtomicReference<List<String>> actionRecorderList
}


@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(FailingCleanupStoreExtension)
@interface FailingStoreUsage {
  MethodKind[] value() default []
}


class FailingCleanupStoreExtension implements IAnnotationDrivenExtension<FailingStoreUsage> {

  final FailingCleanupStoreConfig config

  FailingCleanupStoreExtension(FailingCleanupStoreConfig config) {
    this.config = config
  }

  @Override
  void visitSpecAnnotation(FailingStoreUsage annotation, SpecInfo spec) {
    def specInfo = spec.bottomSpec
    def interceptor = new FailingCleanupStoreInterceptor(annotation, config.cleanupRecorderList.get())
    specInfo.addSharedInitializerInterceptor interceptor
    specInfo.allSharedInitializerMethods*.addInterceptor interceptor
    specInfo.addInterceptor interceptor
    specInfo.addSetupSpecInterceptor interceptor
    specInfo.addInitializerInterceptor interceptor
    specInfo.addSetupInterceptor interceptor
    specInfo.allFixtureMethods*.addInterceptor  interceptor
    specInfo.allFeatures*.addInterceptor interceptor
    specInfo.allFeatures*.addIterationInterceptor interceptor
    specInfo.allFeatures*.featureMethod*.addInterceptor interceptor
    specInfo.addCleanupInterceptor interceptor
    specInfo.addCleanupSpecInterceptor interceptor
  }
}

class FailingCleanupStoreInterceptor implements IMethodInterceptor {

  static final IStore.Namespace NAMESPACE = IStore.Namespace.create(FailingCleanupStoreInterceptor.class)

  final FailingStoreUsage annotation
  final List<MethodKind> cleanupFailures

  FailingCleanupStoreInterceptor(FailingStoreUsage annotation, List<MethodKind> cleanupFailures) {
    this.annotation = annotation
    this.cleanupFailures = cleanupFailures
  }

  @Override
  void intercept(IMethodInvocation invocation) throws Throwable {
    def store = invocation.getStore(NAMESPACE)
    store.put(invocation.method.kind,
      new FailingValue(
        cleanupFailures,
        invocation.method.kind,
        "Failing at $invocation.method.kind - $invocation.method.name",
        annotation.value().contains(invocation.method.kind))
    )
    invocation.proceed()
  }
}

class FailingValue implements AutoCloseable {

  final String value
  final boolean failAtCleanup
  private final MethodKind methodKind
  private final List<MethodKind> cleanupFailures

  FailingValue(List<MethodKind> cleanupFailures, MethodKind methodKind, String value, boolean failAtCleanup) {
    this.cleanupFailures = cleanupFailures
    this.methodKind = methodKind
    this.failAtCleanup = failAtCleanup
    this.value = value
  }

  @Override
  void close() throws Exception {
    cleanupFailures << methodKind
    Assert.that(!failAtCleanup, value)
  }
}

@ConfigurationObject("failingCleanup")
class FailingCleanupStoreConfig {
  AtomicReference<List<MethodKind>> cleanupRecorderList
}
