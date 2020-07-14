package org.spockframework.runtime.extension.builtin;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.WrappedException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author dqyuan
 * @since 2.0
 */
public abstract class TempDirBaseInterceptor extends AbstractMethodInterceptor {
  private static final String TEMP_DIR_PREFIX = "spock";

  private final Class<?> fieldType;
  private final Field reflection;
  private final String parentDir;
  private final boolean reserveAfterTest;
  private Path tempPath;

  TempDirBaseInterceptor(Class<?> fieldType, Field reflection, String parentDir, boolean reserveAfterTest) {
    this.fieldType = fieldType;
    this.reflection = reflection;
    this.parentDir = parentDir;
    this.reserveAfterTest = reserveAfterTest;
  }

  private Path generateTempDir(String parentDir) {
    try {
      if (parentDir == null || "".equals(parentDir)) {
        return Files.createTempDirectory(TEMP_DIR_PREFIX);
      }
      return Files.createTempDirectory(Paths.get(parentDir), TEMP_DIR_PREFIX);
    } catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  void setUp(IMethodInvocation invocation, boolean execute)  throws Throwable {
    tempPath = generateTempDir(parentDir);
    reflection.setAccessible(true);
    reflection.set(invocation.getInstance(), fieldType == Path.class ? tempPath : tempPath.toFile());
    if (execute)
      invocation.proceed();
  }

  void destroy(IMethodInvocation invocation, boolean execute) throws Throwable {
    try {
      if (execute)
        invocation.proceed();
    } finally {
      if (!reserveAfterTest) {
        deleteTempDir();
      }
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
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        dir.toFile().setWritable(true);
        return CONTINUE;
      }
    });
  }

  abstract void install(SpecInfo specInfo);
}
