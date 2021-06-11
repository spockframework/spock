package spock.platform;

import org.apache.maven.surefire.api.testset.*;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.testkit.engine.EventStatistics;
import spock.testkit.testsources.InheritedChildTestCase;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
class InheritedTest extends SpockEngineBase {

  @Test
  void verifyInheritedTests() {
    Consumer<EventStatistics> assertions = stats -> stats.started(3).succeeded(2).failed(1).skipped(1);
    execute(selectClass(InheritedChildTestCase.class), assertions, Arrays.asList(new TestMethodFilter(new TestListResolver("InheritedChildTestCase"))));
  }

  /**
   * Copy of Maven Surefire Plugin's TestMethodFilter -- it is private so it cannot be imported.
   */
  class TestMethodFilter
    implements PostDiscoveryFilter
  {

    private final TestListResolver testListResolver;

    TestMethodFilter( TestListResolver testListResolver )
    {
      this.testListResolver = testListResolver;
    }

    @Override
    public FilterResult apply(TestDescriptor descriptor )
    {
      boolean shouldRun = descriptor.getSource()
        .filter( MethodSource.class::isInstance )
        .map( MethodSource.class::cast )
        .map( this::shouldRun )
        .orElse( true );

      return FilterResult.includedIf( shouldRun );
    }

    private boolean shouldRun( MethodSource source )
    {
      String testClass = TestListResolver.toClassFileName( source.getClassName() );
      String testMethod = source.getMethodName();
      System.out.println("RESOLVING NAME " + testClass + "#" + testMethod);
      return this.testListResolver.shouldRun( testClass, testMethod );
    }
  }

}
