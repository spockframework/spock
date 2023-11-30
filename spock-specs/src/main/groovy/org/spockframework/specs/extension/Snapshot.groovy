package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.PARAMETER])
@ExtensionAnnotation(SnapshotExtension)
@CompileStatic
@interface Snapshot {
  /**
   * The file extension to use for the snapshot files.
   * Defaults to "txt".
   */
  String extension() default "txt"
}
