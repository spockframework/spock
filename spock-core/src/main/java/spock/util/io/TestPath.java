package spock.util.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class TestPath {
  private final Path root;

  public TestPath(Path root) {
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

  public Path createDirectory(String pathToDirectory) throws IOException {
    Path dir = root.resolve(pathToDirectory);
    Files.createDirectories(dir);
    return dir;
  }

  public Path createFile(String pathToFile) throws IOException {
    Path file = root.resolve(pathToFile);
    Files.createDirectories(file.getParent());
    return file;
  }

  public Path createFile(String pathToFile, String content) throws IOException {
    Path file = createFile(pathToFile);
    Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    return file;
  }
}
