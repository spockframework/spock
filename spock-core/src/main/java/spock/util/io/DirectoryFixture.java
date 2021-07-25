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
   * Creates a file object relative to the enclosing fixture or directory,
   * but not the physical File, it will eagerly create parent directories if necessary.
   *
   * @param file the (path and) filename
   * @return the {@link File} object pointing to the file
   * @throws IOException if the parent directories could not be created.
   */
  File file(String file) throws IOException;

  /**
   * Copies a file from classpath using the contextClass to load the resource.
   * <p>
   * This will use the {@link Closure#getOwner()} of the enclosing closure to determine the contextClass.
   *
   * @param resourcePath the path to the resource to load
   * @param targetName the name of the target to use
   * @return the {@link File} pointing to the copied file
   * @throws IOException if the parent directories could not be created or the resource could not be found
   */
  File copyFromClasspath(String resourcePath, String targetName) throws IOException;

  /**
   * Copies a file from classpath using the contextClass to load the resource.
   *
   * @param resourcePath the path to the resource to load
   * @param targetName the name of the target to use
   * @param contextClass the class to use to load the resource
   * @return the {@link File} pointing to the copied file
   * @throws IOException if the parent directories could not be created or the resource could not be found
   */
  File copyFromClasspath(String resourcePath, String targetName, Class<?> contextClass) throws IOException;
}
