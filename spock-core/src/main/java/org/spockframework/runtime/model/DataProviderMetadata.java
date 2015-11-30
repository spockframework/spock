package org.spockframework.runtime.model;

import java.lang.annotation.*;

/**
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataProviderMetadata {
  String LINE = "line";
  String DATA_VARIABLES = "dataVariables";

  int line();
  String[] dataVariables();
}
