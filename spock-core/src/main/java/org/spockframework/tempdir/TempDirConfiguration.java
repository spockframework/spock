package org.spockframework.tempdir;

import org.spockframework.util.Beta;
import spock.config.ConfigurationObject;

import java.nio.file.Path;

/**
 *
 * Configuration settings for the TempDir extension.
 *
 * <p>Example:
 * <pre>
 * tempdir {
 *   // java.nio.Path object, default null,
 *   // which means system property "java.io.tmpdir"
 *   baseDir Paths.get("/tmp")
 *   // boolean, default is system property "spock.tempDir.keep"
 *   keep true
 * }
 * </pre>
 * @author dqyuan
 * @since 2.0
 */
@Beta
@ConfigurationObject("tempdir")
public class TempDirConfiguration {

  /**
   * The parent directory for the temporary folder, default is system property {@code java.io.tmpdir}.
   */
  public Path baseDir = null;

  /**
   * Whether to keep the temp directory or not after test,
   * default is system property {@code spock.tempDir.keep} or false if it is not set.
   */
  public boolean keep = Boolean.getBoolean("spock.tempDir.keep");
}
