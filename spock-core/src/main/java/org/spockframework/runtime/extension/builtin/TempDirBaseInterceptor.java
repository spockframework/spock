package org.spockframework.runtime.extension.builtin;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.WrappedException;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.BiConsumer;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author dqyuan
 * @since 2.0
 */
public abstract class TempDirBaseInterceptor extends AbstractMethodInterceptor {
  private static final String TEMP_DIR_PREFIX = "spock";

  private final Class<?> fieldType;
  private final BiConsumer<Object, Object> fieldSetter;
  private final String parentDir;
  private final boolean reserveAfterTest;
  private Path tempPath;

  TempDirBaseInterceptor(Class<?> fieldType, BiConsumer<Object, Object> fieldSetter,
                         String parentDir, boolean reserveAfterTest) {
    this.fieldType = fieldType;
    this.fieldSetter = fieldSetter;
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

  protected void setUp(Object specInst) {
    tempPath = generateTempDir(parentDir);
    fieldSetter.accept(specInst, fieldType.isAssignableFrom(Path.class) ?
      tempPath : tempPath.toFile());
  }

  protected void destroy() throws IOException {
    if (!reserveAfterTest) {
      deleteTempDir();
    }
  }

  protected void invoke(IMethodInvocation invocation) throws Throwable {
    setUp(invocation.getInstance());
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
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        dir.toFile().setWritable(true);
        return CONTINUE;
      }
    });
  }

  abstract void install(SpecInfo specInfo);
}
