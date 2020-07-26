package org.spockframework.runtime.extension.builtin;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.MethodKind;
import org.spockframework.util.Beta;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirInterceptor implements IMethodInterceptor {
  private static final String TEMP_DIR_PREFIX = "spock";

  private final Class<?> fieldType;
  private final FieldInfo fieldInfo;
  private final Path parentDir;
  private final MethodKind interceptPoint;
  private final boolean keep;

  private Path tempPath;

  TempDirInterceptor(Class<?> fieldType, FieldInfo fieldInfo,
                     MethodKind interceptPoint, Path parentDir,
                     boolean keep) {
    this.fieldType = fieldType;
    this.fieldInfo = fieldInfo;
    this.parentDir = parentDir;
    this.interceptPoint = interceptPoint;
    this.keep = keep;
  }

  private Path generateTempDir(IMethodInvocation invocation) throws IOException {
    String prefix = TEMP_DIR_PREFIX + "_" + ((invocation.getIteration() == null) ?
      invocation.getSpec().getName() : invocation.getIteration().getName())
      .replaceAll("[^a-zA-Z0-9_.-]++", "_") + "_" + fieldInfo.getName();
    if (parentDir == null) {
      return Files.createTempDirectory(prefix);
    }
    return Files.createTempDirectory(parentDir, prefix);
  }

  protected void setUp(IMethodInvocation invocation) throws IOException {
    tempPath = generateTempDir(invocation);
    fieldInfo.writeValue(invocation.getInstance(), fieldType.isAssignableFrom(Path.class) ?
      tempPath : tempPath.toFile());
  }

  protected void destroy() throws IOException {
    if (!keep) {
      deleteTempDir();
    }
  }

  private void invoke(IMethodInvocation invocation) throws Throwable {
    setUp(invocation);
    try {
      invocation.proceed();
    } finally {
      destroy();
    }
  }

  private void deleteTempDir() throws IOException {
    if (Files.notExists(tempPath)) {
      return;
    }

    if (ResourceGroovyMethods.deleteDir(tempPath.toFile())) {
      return;
    }

    tryMakeWritable();
    ResourceGroovyMethods.deleteDir(tempPath.toFile());
  }

  private void tryMakeWritable() throws IOException {
    Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        file.toFile().setWritable(true);
        return CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        dir.toFile().setWritable(true);
        return CONTINUE;
      }
    });
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    if (invocation.getMethod().getKind() == interceptPoint) {
      invoke(invocation);
    }
  }
}
