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
 *   baseDir Paths.get(System.getProperty("java.io.tmpdir")) // java.nio.Path object, default null, which means {@code System.getProperty("java.io.tmpdir")}
 *   keep true // boolean, default {@code Boolean.getBoolean("spock.tempDir.keep")}, which means not keep temp directory after test
 * }
 * </pre>
 * @author dqyuan
 * @since 2.0
 */
@Beta
@ConfigurationObject("tempdir")
public class TempDirConfiguration {

  /**
   * The parent directory for the temporary folder, default is system temp directory.
   */
  public Path baseDir = null;

  /**
   * Whether to keep the temp directory or not after test, default is not.
   * */
  public boolean keep = Boolean.getBoolean("spock.tempDir.keep");
}
