package spock.util.io;

import org.spockframework.util.Beta;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

import groovy.lang.*;
import org.jetbrains.annotations.NotNull;

/**
 * FsFixture can be used to create temporary directories and files.
 *
 * It is intended to be used with {@link spock.lang.TempDir}.
 *
 * @author Leonard Brünings
 * @since 2.2
 */
@Beta
public class FsFixture implements DirectoryFixture {
  private final Path currentPath;
  private final Class<?> contextClass;

  public FsFixture(Path currentPath) {
    this.currentPath = currentPath;
    this.contextClass = FsFixture.class;
  }

  private FsFixture(Path currentPath, Class<?> contextClass) {
    this.currentPath = currentPath;
    this.contextClass = contextClass;
  }

  /**
   * @return the path of the this fixture
   */
  public Path getCurrentPath() {
    return currentPath;
  }

  /**
   * A shorthand for {@code getCurrentPath().resolve(path)}
   *
   * @param path the path to resolve relative to currentPath
   * @return the resolved path
   */
  public Path resolve(String path) {
    return currentPath.resolve(path);
  }

  /**
   * A shorthand for {@code getCurrentPath().resolve(path)}
   *
   * @param path the path to resolve relative to currentPath
   * @return the resolved path
   */
  public Path resolve(Path path) {
    return currentPath.resolve(path);
  }

  /**
   * Basically the same as {@link DirectoryFixture#dir(String, Closure)}, but with the current root as base.
   */
  public void create(@DelegatesTo(value = DirectoryFixture.class, strategy = Closure.DELEGATE_FIRST)
                     Closure<?> dirSpec) throws IOException {
    callSpec(dirSpec, currentPath);
  }

  @Override
  public Path dir(String dir) throws IOException {
    Path result = currentPath.resolve(dir);
    Files.createDirectories(result);
    return result;
  }

  @Override
  public Path dir(String dir, Closure<?> dirSpec) throws IOException {
    Path result = dir(dir);
    callSpec(dirSpec, result);
    return result;
  }

  @Override
  public Path file(String file) throws IOException {
    Path result = currentPath.resolve(file);
    Files.createDirectories(result.getParent());
    return result;
  }

  @Override
  public Path copyFromClasspath(String resourcePath) throws IOException {
    return copyFromClasspath(resourcePath, getTargetName(resourcePath));
  }

  @NotNull
  private String getTargetName(String resourcePath) {
    return resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
  }

  @Override
  public Path copyFromClasspath(String resourcePath, String targetName) throws IOException {
    return copyFromClasspath(resourcePath, targetName, contextClass);
  }

  @Override
  public Path copyFromClasspath(String resourcePath, Class<?> contextClass) throws IOException {
    return copyFromClasspath(resourcePath, getTargetName(resourcePath), contextClass);
  }

  @Override
  public Path copyFromClasspath(String resourcePath, String targetName, Class<?> contextClass) throws IOException {
    URL resource = contextClass.getResource(resourcePath);
    if (resource == null) {
      throw new IOException("Could not find resource: " + resourcePath);
    }
    return copyResource(resource, file(targetName));
  }

  @NotNull
  private Path copyResource(URL resource, Path file) throws IOException {
    try (InputStream inputStream = resource.openStream()) {
      Files.copy(inputStream, file);
    }
    return file;
  }

  private void callSpec(Closure<?> dirSpec, Path path) {
    Class<?> newContextClass = contextClass;
    Object owner = dirSpec.getOwner();
    if (owner instanceof Class) {
      newContextClass = (Class<?>)owner;
    } else if (owner != null) {
      newContextClass = owner.getClass();
    }
    FsFixture fsFixture = new FsFixture(path, newContextClass);
    dirSpec.setResolveStrategy(Closure.DELEGATE_FIRST);
    dirSpec.setDelegate(fsFixture);
    dirSpec.call();
  }
}
