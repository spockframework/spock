package org.spockframework.runtime.model;

import org.spockframework.util.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since 2.0
 */
@Beta
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataProcessorMetadata {
  String DATA_VARIABLES = "dataVariables";

  String[] dataVariables();
}
