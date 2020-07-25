package org.spockframework.runtime.extension.builtin;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Beta;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public abstract class TempDirBaseInterceptor extends AbstractMethodInterceptor {
  private static final String TEMP_DIR_PREFIX = "spock";

  private final Class<?> fieldType;
  private final FieldInfo fieldInfo;
  private final String parentDir;
  private final boolean keep;
  private Path tempPath;

  TempDirBaseInterceptor(Class<?> fieldType, FieldInfo fieldInfo,
                         String parentDir, boolean keep) {
    this.fieldType = fieldType;
    this.fieldInfo = fieldInfo;
    this.parentDir = parentDir;
    this.keep = keep;
  }

  private Path generateTempDir() throws IOException {
    if (parentDir == null || "".equals(parentDir)) {
      return Files.createTempDirectory(TEMP_DIR_PREFIX);
    }
    return Files.createTempDirectory(Paths.get(parentDir), TEMP_DIR_PREFIX);
  }

  protected void setUp(Object specInst) throws IOException {
    tempPath = generateTempDir();
    fieldInfo.writeValue(specInst, fieldType.isAssignableFrom(Path.class) ?
      tempPath : tempPath.toFile());
  }

  protected void destroy() throws IOException {
    if (!keep) {
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
