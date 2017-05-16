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
  String PARAMETERS = "parameters";

  int line();
  String[] dataVariables();
  String[] parameters();
}
