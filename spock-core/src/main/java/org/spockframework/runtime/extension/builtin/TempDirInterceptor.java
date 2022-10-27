package org.spockframework.runtime.extension.builtin;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.IStore;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.ParameterInfo;
import org.spockframework.util.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirInterceptor implements IMethodInterceptor {

  private static final IStore.Namespace NAMESPACE = IStore.Namespace.create(TempDirInterceptor.class);
  private static final String TEMP_DIR_PREFIX = "spock";
  private static final Pattern VALID_CHARS = Pattern.compile("[^a-zA-Z0-9_.-]++");

  private final IThrowableBiConsumer<IMethodInvocation, Path, Exception> valueSetter;
  private final String name;
  private final Path parentDir;
  private final boolean keep;

  private TempDirInterceptor(
    IThrowableBiConsumer<IMethodInvocation, Path, Exception> valueSetter,
    String name,
    Path parentDir,
    boolean keep) {

    this.valueSetter = valueSetter;
    this.name = name;
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
    return prefix.append('_').append(name).toString();
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
    valueSetter.accept(invocation, tempPath);
    return tempPath;
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    Path path = setUp(invocation);
    invocation.getStore(NAMESPACE).put(path, new TempDirContainer(path, keep));
    invocation.proceed();
  }


  private static IThrowableFunction<Path, ?, Exception> createPathToTypeMapper(Class<?> targetType) {
    if (targetType.isAssignableFrom(Path.class) || Object.class.equals(targetType)) {
      return p -> p;
    }
    if (targetType.isAssignableFrom(File.class)) {
      return Path::toFile;
    }

    try {
      return targetType.getConstructor(Path.class)::newInstance;
    } catch (NoSuchMethodException ignore) {
      // fall through
    }
    try {
      Constructor<?> constructor = targetType.getConstructor(File.class);
      return path -> constructor.newInstance(path.toFile());
    } catch (NoSuchMethodException ignore) {
      // fall through
    }
    throw new InvalidSpecException("@TempDir can only be used on File, Path, untyped field, " +
      "or class that takes Path or File as single constructor argument.");
  }

  private static void deleteTempDir(Path tempPath) throws IOException {
    if (Files.notExists(tempPath)) {
      return;
    }

    if (ResourceGroovyMethods.deleteDir(tempPath.toFile())) {
      return;
    }

    tryMakeWritable(tempPath);
    ResourceGroovyMethods.deleteDir(tempPath.toFile());
  }

  private static void tryMakeWritable(Path tempPath) throws IOException {
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

  static TempDirInterceptor forField(FieldInfo fieldInfo, Path parentDir, boolean keep) {
    IThrowableFunction<Path, ?, Exception> typeMapper = createPathToTypeMapper(fieldInfo.getType());
    return new TempDirInterceptor(
      (invocation, path) -> fieldInfo.writeValue(invocation.getInstance(), typeMapper.apply(path)),
      fieldInfo.getName(),
      parentDir,
      keep);
  }

  static TempDirInterceptor forParameter(ParameterInfo parameterInfo, Path parentDir, boolean keep) {
    IThrowableFunction<Path, ?, Exception> typeMapper = createPathToTypeMapper(parameterInfo.getReflection().getType());
    return new TempDirInterceptor(
      (IMethodInvocation invocation, Path path) -> {
        invocation.resolveArgument(parameterInfo.getIndex(), typeMapper.apply(path));
      },
      parameterInfo.getName(),
      parentDir,
      keep);
  }

  static class TempDirContainer implements AutoCloseable {
    private final Path path;
    private final boolean keep;

    TempDirContainer(Path path, boolean keep) {
      this.path = path;
      this.keep = keep;
    }

    @Override
    public void close() {
      if (!keep) {
        try {
          deleteTempDir(path);
        } catch (IOException e) {
          ExceptionUtil.sneakyThrow(e);
        }
      }
    }
  }
}
