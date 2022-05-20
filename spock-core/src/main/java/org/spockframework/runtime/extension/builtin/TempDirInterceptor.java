package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.ErrorCollector;
import org.spockframework.runtime.ErrorRethrower;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.spockframework.util.ExceptionUtil;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirInterceptor implements IMethodInterceptor {
  private static final String TEMP_DIR_PREFIX = "spock";
  private static final Pattern VALID_CHARS = Pattern.compile("[^a-zA-Z0-9_.-]++");

  private final IThrowableFunction<Path, ?, Exception> pathToFieldMapper;
  private final FieldInfo fieldInfo;
  private final Path parentDir;
  private final boolean keep;

  TempDirInterceptor(IThrowableFunction<Path, ?, Exception> pathToFieldMapper, FieldInfo fieldInfo,
                     Path parentDir, boolean keep) {
    this.pathToFieldMapper = pathToFieldMapper;
    this.fieldInfo = fieldInfo;
    this.parentDir = parentDir;
    this.keep = keep;
  }

  private String dirPrefix(IMethodInvocation invocation) {
    StringBuilder prefix = new StringBuilder(TEMP_DIR_PREFIX);
    prefix.append('_');
    // for shared field, no iteration is set, so use the spec name
    // otherwise use the iteration name
    String displayName = (invocation.getIteration() == null)
      ? invocation.getSpec().getDisplayName()
      : invocation.getIteration().getDisplayName();
    prefix.append(VALID_CHARS.matcher(displayName).replaceAll("_"));
    if (prefix.length() > 25) {
      prefix.setLength(25);
    }
    if (invocation.getIteration() != null) {
      prefix.append('_').append(invocation.getIteration().getIterationIndex());
    }
    return prefix.append('_').append(fieldInfo.getName()).toString();
  }

  private Path generateTempDir(IMethodInvocation invocation) throws IOException {
    String prefix = dirPrefix(invocation);
    if (parentDir == null) {
      return Files.createTempDirectory(prefix);
    }
    if (!Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }
    return Files.createTempDirectory(parentDir, prefix);
  }

  protected Path setUp(IMethodInvocation invocation) throws Exception {
    Path tempPath = generateTempDir(invocation);
    fieldInfo.writeValue(invocation.getInstance(), pathToFieldMapper.apply(tempPath));
    return tempPath;
  }

  protected void destroy(Path path) {
    if (!keep) {
      try {
        deleteTempDir(path);
      } catch (IOException e) {
        ExceptionUtil.sneakyThrow(e);
      }
    }
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    Path path = setUp(invocation);
    if(fieldInfo.isShared()) {
      invocation.getSpec().addCleanupSpecInterceptor(cleanupInvocation -> {
        try {
          cleanupInvocation.proceed();
        } finally {
          destroy(path);
        }
      });
    } else {
      invocation.getIteration().addCleanup(() -> destroy(path));
    }
    invocation.proceed();
  }

  private void deleteTempDir(Path tempPath) throws IOException {
    if (Files.notExists(tempPath)) {
      return;
    }

    if (ResourceGroovyMethods.deleteDir(tempPath.toFile())) {
      return;
    }

    tryMakeWritable(tempPath);
    ResourceGroovyMethods.deleteDir(tempPath.toFile());
  }

  private void tryMakeWritable(Path tempPath) throws IOException {
    Files.walkFileTree(tempPath, MakeWritableVisitor.INSTANCE);
  }

  private static class MakeWritableVisitor extends SimpleFileVisitor<Path> {
    static MakeWritableVisitor INSTANCE = new MakeWritableVisitor();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      file.toFile().setWritable(true);
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      dir.toFile().setWritable(true);
      return CONTINUE;
    }
  }
}
