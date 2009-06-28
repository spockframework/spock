package org.spockframework.runtime.model;

import java.lang.annotation.*;

/**
 * @author Peter Niederwieser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataProviderMetadata {
  String LINE = "line";
  String COLUMN = "column";
  String DATA_VARIABLES = "dataVariables";
  
  int line();
  int column();
  String[] dataVariables();
}
