package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.WrappedException;
import spock.lang.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author: dqyuan
 * @date: 2020/07/12
 */
public class TempDirExtension implements IAnnotationDrivenExtension<TempDir> {

  private static final String TEMP_DIR_PREFIX = "spock";

  @Override
  public void visitFieldAnnotation(TempDir annotation, FieldInfo field) {
    Class<?> fieldType = field.getType();
    if (!File.class.equals(fieldType) && !Path.class.equals(fieldType)) {
      throw new InvalidSpecException("@TempDir can only be used on File field or Path field");
    }
    DirInterceptor interceptor = field.isShared()?
      new SharedInterceptor(fieldType, field.getReflection()):
      new FeatureInterceptor(fieldType, field.getReflection());
    interceptor.install(field.getParent());
  }

  private static Path generateTempDir() {
    try {
      return Files.createTempDirectory(TEMP_DIR_PREFIX);
    } catch (IOException e) {
      throw new WrappedException(e);
    }
  }

  private abstract static class DirInterceptor extends AbstractMethodInterceptor {
    private final Class<?> fieldType;
    private final Field reflection;
    private Path tempPath;

    DirInterceptor(Class<?> fieldType, Field reflection) {
      this.fieldType = fieldType;
      this.reflection = reflection;
    }

    void setUp(IMethodInvocation invocation)  throws Throwable {
      tempPath = generateTempDir();
      reflection.setAccessible(true);
      reflection.set(invocation.getInstance(), fieldType == Path.class? tempPath: tempPath.toFile());
      invocation.proceed();
    }

    void destroy(IMethodInvocation invocation) throws Throwable {
      try {
        invocation.proceed();
      } finally {
        deleteTempDir();
      }
    }

    private void deleteTempDir() throws IOException {
      if (Files.notExists(tempPath)) {
        return;
      }

      Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
          return deleteAndContinue(file);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
          return deleteAndContinue(dir);
        }

        private FileVisitResult deleteAndContinue(Path path) {
          try {
            Files.delete(path);
          } catch (NoSuchFileException | DirectoryNotEmptyException  ignore) {
          } catch (IOException exception) {
            makeWritableAndTryToDeleteAgain(path, exception);
          }
          return CONTINUE;
        }

        private void makeWritableAndTryToDeleteAgain(Path path, IOException exception) {
          try {
            tryToMakeParentDirsWritable(path);
            makeWritable(path);
            Files.delete(path);
          } catch (Exception ignore) {
          }
        }

        private void tryToMakeParentDirsWritable(Path path) {
          Path relativePath = tempPath.relativize(path);
          Path parent = tempPath;
          for (int i = 0; i < relativePath.getNameCount(); i++) {
            boolean writable = parent.toFile().setWritable(true);
            if (!writable) {
              break;
            }
            parent = parent.resolve(relativePath.getName(i));
          }
        }

        private void makeWritable(Path path) {
          path.toFile().setWritable(true);
        }
      });
    }

    abstract void install(SpecInfo specInfo);
  }

  private static class SharedInterceptor extends DirInterceptor {
    SharedInterceptor(Class<?> fieldType, Field reflection) {
      super(fieldType, reflection);
    }

    @Override
    void install(SpecInfo specInfo) {
      specInfo.addSetupSpecInterceptor(this);
      specInfo.addCleanupSpecInterceptor(this);
    }

    @Override
    public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
      setUp(invocation);
    }

    @Override
    public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
      destroy(invocation);
    }
  }

  private static class FeatureInterceptor extends DirInterceptor {
    FeatureInterceptor(Class<?> fieldType, Field reflection) {
      super(fieldType, reflection);
    }

    @Override
    void install(SpecInfo specInfo) {
      specInfo.addSetupInterceptor(this);
      specInfo.addCleanupInterceptor(this);
    }

    @Override
    public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
      setUp(invocation);
    }

    @Override
    public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
      destroy(invocation);
    }
  }
}
