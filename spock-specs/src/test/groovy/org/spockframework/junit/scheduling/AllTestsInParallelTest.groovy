package org.spockframework.junit.scheduling

import org.junit.runner.JUnitCore
import org.junit.runner.Result
import org.junit.runner.notification.RunListener
import org.spockframework.report.log.ReportLogConfigurationSpec
import org.spockframework.runtime.ConfigurationScriptLoaderSpec
import org.spockframework.runtime.RunContextSpec
import org.spockframework.runtime.StandardStreamsCapturerSpec
import org.spockframework.smoke.extension.ReportLogExtensionSpec
import org.spockframework.smoke.extension.RestoreSystemPropertiesExtension
import org.spockframework.smoke.extension.TimeoutExtensionThreadAffinity
import org.spockframework.smoke.extension.UseRestoreSystemPropertiesOnSpecClass
import org.spockframework.smoke.mock.GroovySpiesThatAreGlobal
import org.spockframework.smoke.mock.InvokingMocksFromMultipleThreads
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicReference

class AllTestsInParallelTest extends Specification {
  private static final Set<Class> excludeFromTest = [
    AllTestsInParallelTest.class, // prevent recursion
    RunContextSpec.class, // this spec tests top level naming, will not work under runner.withNewContext
    ReportLogExtensionSpec.class, // LogExtension will not work in parallel mode
    ReportLogConfigurationSpec.class,
    ConfigurationScriptLoaderSpec.class, // sets system properties
    StandardStreamsCapturerSpec.class, // sets System.out
    GroovySpiesThatAreGlobal.class, // it use List as subject to mock, and mocks are global
    TimeoutExtensionThreadAffinity.class, // it checks that execution is performed in "main" thread. It does not have sense in parallel mode.
    RestoreSystemPropertiesExtension.class, // use system properties
    UseRestoreSystemPropertiesOnSpecClass.class, // use system properties
    InvokingMocksFromMultipleThreads.class, // starts new threads, can fail on slow boxes
  ]

  def "all spock-spec tests in parallel mode"() {

    def listener = Mock(RunListener)
    def jUnitCore = new JUnitCore()
    jUnitCore.addListener(listener)

    when:
    def runner = new EmbeddedSpecRunner()
    AtomicReference<Result> resultAR = new AtomicReference<>()
    runner.withNewContext {
      def parallelComputer = new ParallelComputerWithFixedPool(10)
      def testClasses = getTestClasses("org.spockframework")
      def testClassesAsArray = testClasses.toArray(new Class<?>[0])
      def result = jUnitCore.run(parallelComputer, testClassesAsArray)
      resultAR.set(result)
    }

    then:
    resultAR.get().failures.isEmpty()
  }

  private static List<Class> getTestClasses(String packageName) {
    ArrayList<Class> testClasses = new ArrayList<Class>();
    def allClasses = getClasses(packageName)
    for (Class aClass : allClasses) {
      if (Specification.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers()) && !excludeFromTest.contains(aClass)) {
        testClasses.add(aClass);
      }
    }
    return testClasses;
  }

  /**
   * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
   *
   * @param packageName The base package
   * @return The classes
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private static List<Class> getClasses(String packageName)
    throws ClassNotFoundException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<File>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    ArrayList<Class> classes = new ArrayList<Class>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }
    return classes;
  }

/**
 * Recursive method used to find all classes in a given directory and subdirs.
 *
 * @param directory The base directory
 * @param packageName The package name for classes found inside the base directory
 * @return The classes
 * @throws ClassNotFoundException
 */
  private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
    List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }
}
