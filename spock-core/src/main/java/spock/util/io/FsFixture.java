package spock.util.io;

import org.spockframework.util.Beta;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

import groovy.lang.*;
import org.jetbrains.annotations.NotNull;

@Beta
public class FsFixture implements DirectoryFixture {
  private final Path root;

  public FsFixture(Path root) {
    this.root = root;
  }

  public Path getRoot() {
    return root;
  }

  public Path resolve(String path) {
    return root.resolve(path);
  }

  public Path resolve(Path path) {
    return root.resolve(path);
  }

  /**
   * Basically the same as {@link DirectoryFixture#dir(String, Closure)}, but with the current root as base.
   */
  public void create(@DelegatesTo(value = DirectoryFixture.class, strategy = Closure.DELEGATE_FIRST)
                       Closure<?> dirSpec) throws IOException {
    callSpec(dirSpec, this);
  }

  @Override
  public Path dir(String dir) throws IOException {
    Path result = root.resolve(dir);
    Files.createDirectories(result);
    return result;
  }

  @Override
  public Path dir(String dir, Closure<?> dirSpec) throws IOException {
    Path result = dir(dir);
    callSpec(dirSpec, new FsFixture(result));
    return result;
  }

  @Override
  public File file(String file) throws IOException {
    Path result = root.resolve(file);
    Files.createDirectories(result.getParent());
    return result.toFile();
  }

  @Override
  public File copyFromClasspath(String resourcePath, String targetName) throws IOException {
    URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
    if (resource == null) {
      throw new IOException("Could not find resource: " + resourcePath);
    }
    return copyResource(resource, file(targetName));
  }

  @Override
  public File copyFromClasspath(String resourcePath, String targetName, Class<?> contextClass) throws IOException {
    URL resource = contextClass.getResource(resourcePath);
    if (resource == null) {
      throw new IOException("Could not find resource: " + resourcePath);
    }
    return copyResource(resource, file(targetName));
  }

  @NotNull
  private File copyResource(URL resource, File file) throws IOException {
    try (InputStream inputStream = resource.openStream()) {
      Files.copy(inputStream, file.toPath());
    }
    return file;
  }

  private void callSpec(Closure<?> dirSpec, FsFixture fsFixture) {
    dirSpec.setResolveStrategy(Closure.DELEGATE_FIRST);
    dirSpec.setDelegate(fsFixture);
    dirSpec.call();
  }
}
