package org.spockframework.util;

import java.lang.annotation.Annotation;

/**
 * The utility class to deal with the JUnit 4 annotations in Spock in the way which allows to use Spock 2
 * without junit4.jar at runtime.
 *
 * @since 3.0.0
 */
@SuppressWarnings("unchecked")
public class JUnit4LegacyUtil {
  private static final boolean IS_JUNIT4_AVAILABLE =
    ReflectionUtil.loadClassIfAvailable("org.spockframework.junit4.ExceptionAdapterExtension") != null;

  public static final Class<? extends Annotation> BEFORE_ANNOTATION =
    (Class<? extends Annotation>) ReflectionUtil.loadClassIfAvailable("org.junit.Before");
  public static final Class<? extends Annotation> AFTER_ANNOTATION =
    (Class<? extends Annotation>) ReflectionUtil.loadClassIfAvailable("org.junit.After");
  public static final Class<? extends Annotation> BEFORE_CLASS_ANNOTATION =
    (Class<? extends Annotation>) ReflectionUtil.loadClassIfAvailable("org.junit.BeforeClass");
  public static final Class<? extends Annotation> AFTER_CLASS_ANNOTATION =
    (Class<? extends Annotation>) ReflectionUtil.loadClassIfAvailable("org.junit.AfterClass");

  public static boolean isJunit4SupportAvailable() {
    return IS_JUNIT4_AVAILABLE;
  }
}
