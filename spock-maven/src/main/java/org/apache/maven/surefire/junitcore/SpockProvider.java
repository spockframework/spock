/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.surefire.junitcore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.maven.surefire.common.junit4.JUnit4RunListenerFactory;
import org.apache.maven.surefire.common.junit4.JUnit4TestChecker;
import org.apache.maven.surefire.common.junit48.FilterFactory;
import org.apache.maven.surefire.common.junit48.JUnit48Reflector;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.util.RunOrderCalculator;
import org.apache.maven.surefire.util.ScannerFilter;
import org.apache.maven.surefire.util.TestsToRun;
import org.apache.maven.surefire.util.internal.StringUtils;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * Adaptation of {@link org.apache.maven.surefire.junitcore.JUnitCoreProvider}
 * to Spock. Not meant to run JUnit tests.
 *
 * @author Kristian Rosenvold
 */
@SuppressWarnings( { "UnusedDeclaration" } )
public class SpockProvider
    extends AbstractProvider
{
  private final ClassLoader testClassLoader;

  private final DirectoryScanner directoryScanner;

  private final JUnitCoreParameters jUnitCoreParameters;

  private final ScannerFilter scannerFilter;

  private final List<org.junit.runner.notification.RunListener> customRunListeners;

  private final ProviderParameters providerParameters;



  private TestsToRun testsToRun;

  private JUnit48Reflector jUnit48Reflector;

  private RunOrderCalculator runOrderCalculator;

  private String requestedTestMethod;

  public SpockProvider( ProviderParameters providerParameters )
  {
    this.providerParameters = providerParameters;
    this.testClassLoader = providerParameters.getTestClassLoader();
    this.directoryScanner = providerParameters.getDirectoryScanner();
    this.runOrderCalculator = providerParameters.getRunOrderCalculator();
    this.jUnitCoreParameters = new JUnitCoreParameters( providerParameters.getProviderProperties() );
    this.scannerFilter = new JUnit4TestChecker( testClassLoader );
    this.requestedTestMethod = providerParameters.getTestRequest().getRequestedTestMethod();

    customRunListeners = JUnit4RunListenerFactory.
        createCustomListeners( providerParameters.getProviderProperties().getProperty( "listener" ) );
    jUnit48Reflector = new JUnit48Reflector( testClassLoader );
  }

  public Boolean isRunnable()
  {
    return Boolean.TRUE;
  }

  public Iterator getSuites()
  {
    final Filter filter = jUnit48Reflector.isJUnit48Available() ? createJUnit48Filter() : null;
    testsToRun = getSuitesAsList( filter );
    return testsToRun.iterator();
  }

  public RunResult invoke( Object forkTestSet )
      throws TestSetFailedException, ReporterException
  {
    final String message = "Concurrency config is " + jUnitCoreParameters.toString() + "\n";
    final ReporterFactory reporterFactory = providerParameters.getReporterFactory();

    final ConsoleLogger consoleLogger = providerParameters.getConsoleLogger();
    consoleLogger.info( message );

    Filter filter = jUnit48Reflector.isJUnit48Available() ? createJUnit48Filter() : null;

    if ( testsToRun == null )
    {
      testsToRun = forkTestSet == null ? getSuitesAsList( filter ) : TestsToRun.fromClass( (Class) forkTestSet );
    }

    if (testsToRun.size() == 0)
    {
      filter = null;
    }

    final Map<String, TestSet> testSetMap = new ConcurrentHashMap<String, TestSet>();

    RunListener listener = ConcurrentReporterManager.createInstance( testSetMap, reporterFactory,
        jUnitCoreParameters.isParallelClasses(),
        jUnitCoreParameters.isParallelBoth(),
        consoleLogger );

    ConsoleOutputCapture.startCapture( (ConsoleOutputReceiver) listener );

    org.junit.runner.notification.RunListener jUnit4RunListener = new JUnitCoreRunListener( listener, testSetMap );
    customRunListeners.add( 0, jUnit4RunListener );

    JUnitCoreWrapper.execute( testsToRun, jUnitCoreParameters, customRunListeners, filter );
    return reporterFactory.close();
  }

  @SuppressWarnings( "unchecked" )
  private TestsToRun getSuitesAsList( Filter filter )
  {
    List<Class<?>> res = new ArrayList<Class<?>>( 500 );
    TestsToRun max = scanClassPath();
    if ( filter == null )
    {
      return max;
    }

    Iterator<Class<?>> it = max.iterator();
    while ( it.hasNext() )
    {
      Class<?> clazz = it.next();
      boolean isCategoryAnnotatedClass = jUnit48Reflector.isCategoryAnnotationPresent( clazz );
      Description d = Description.createSuiteDescription( clazz );
      if ( filter.shouldRun( d ) )
      {
        res.add( clazz );
      }
      else
      {
        for ( Method method : clazz.getMethods() )
        {
          final Description testDescription =
              Description.createTestDescription( clazz, method.getName(), method.getAnnotations() );
          if ( filter.shouldRun( testDescription ) )
          {
            res.add( clazz );
            break;
          }
        }
      }
    }
    return new TestsToRun( res );
  }

  private Filter createJUnit48Filter()
  {
    final FilterFactory filterFactory = new FilterFactory( testClassLoader );
    return isMethodFilterSpecified() ?
        filterFactory.createMethodFilter( requestedTestMethod ) :
        filterFactory.createGroupFilter( providerParameters.getProviderProperties() );
  }

  private TestsToRun scanClassPath()
  {
    final TestsToRun scanned = directoryScanner.locateTestClasses( testClassLoader, scannerFilter );
    return  runOrderCalculator.orderTestClasses(  scanned );
  }

  private boolean isMethodFilterSpecified()
  {
    return !StringUtils.isBlank( requestedTestMethod );
  }
}
