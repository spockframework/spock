package org.spockframework.runtime.model.parallel;


import org.spockframework.util.Beta;

/**
 * Common resource names for synchronizing test execution.
 *
 * @see spock.lang.ResourceLock
 * @see ExclusiveResource
 * @since 2.0
 */
@Beta
public final class Resources {

  /**
   * Represents Java's system properties.
   *
   * @see System#getProperties()
   * @see System#setProperties(java.util.Properties)
   */
  public static final String SYSTEM_PROPERTIES = "java.lang.System.properties";

  /**
   * Represents the standard output stream of the current process.
   *
   * @see System#out
   * @see System#setOut(java.io.PrintStream)
   */
  public static final String SYSTEM_OUT = "java.lang.System.out";

  /**
   * Represents the standard error stream of the current process.
   *
   * @see System#err
   * @see System#setErr(java.io.PrintStream)
   */
  public static final String SYSTEM_ERR = "java.lang.System.err";

  /**
   * Represents the default locale for the current instance of the JVM.
   *
   * @see java.util.Locale#setDefault(java.util.Locale)
   */
  public static final String LOCALE = "java.util.Locale.default";

  /**
   * Represents the default time zone for the current instance of the JVM.
   *
   * @see java.util.TimeZone#setDefault(java.util.TimeZone)
   */
  public static final String TIME_ZONE = "java.util.TimeZone.default";

  /**
   * Represents the groovy metaclass registry.
   *
   * @see groovy.lang.MetaClassRegistry
   * @see groovy.lang.GroovySystem#getMetaClassRegistry()
   */
  public static final String META_CLASS_REGISTRY = "groovy.lang.GroovySystem.metaClassRegistry";

  private Resources() {
    /* no-op */
  }

}
