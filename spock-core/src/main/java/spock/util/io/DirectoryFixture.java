package spock.util.io;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

import groovy.lang.*;

public interface DirectoryFixture {
  /**
   * Creates a directory, creates the ancestors as necessary.
   *
   * @param dir path to the directory, can either be a single level or multilevel
   * @return the path to the directory
   * @throws IOException if the directories could not be created.
   */
  Path dir(String dir) throws IOException;

  /**
   * Creates a directory, creates the ancestors as necessary.
   *
   * Accepts a {@code dirSpec} closure, that can create files and directories in the scope of this one.
   *
   * Example:
   * <pre><code>
   *   dir('src') {
   *     dir('main') {
   *       dir('groovy') {
   *         file('HelloWorld.java') << 'println "Hello World"'
   *       }
   *     }
   *     dir('test/resources') {
   *       file('META-INF/MANIFEST.MF') << 'bogus entry'
   *     }
   *   }
   * </code></pre>
   *
   * @param dir path to the directory, can either be a single level or multilevel
   * @param dirSpec a closure within the scope of this directory.
   * @return the path to the directory
   * @throws IOException if the directories could not be created.
   */
  Path dir(String dir,
           @DelegatesTo(value = DirectoryFixture.class, strategy = Closure.DELEGATE_FIRST)
             Closure<?> dirSpec)
    throws IOException;

  /**
   * Creates a file object, but not the physical File, it will create parent directories if necessary.
   *
   * @param file the (path and) filename
   * @return the {@link File} object pointing to the file
   * @throws IOException if the parent directories could not be created.
   */
  File file(String file) throws IOException;


  File copyFromClasspath(String resourcePath, String targetName) throws IOException;

  File copyFromClasspath(String resourcePath, String targetName, Class<?> contextClass) throws IOException;
}
