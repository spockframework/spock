package org.spockframework.buildsupport;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.objectweb.asm.ClassReader;

/**
 * Finds JUnit 4 test cases. More specifically, finds all class files that are
 * either annotated with @org.junit.runner.RunWith, or have at least one method
 * annotated with @org.junit.Test.
 *
 * @author Peter Niederwieser
 */
public class JUnit4TestClassFinder {
  public List<File> findTestClasses(File baseDir) throws IOException {
    if (!baseDir.isDirectory())
      throw new FileNotFoundException(String.format("directory %s not found", baseDir));

    List<File> testCases = new ArrayList<File>();
    doFindTestCases(baseDir, testCases);
    Collections.sort(testCases);
    return testCases;
  }

  private void doFindTestCases(File dir, List<File> testCases) throws IOException {
    for (File path: dir.listFiles())
      if (path.isDirectory())
        doFindTestCases(path, testCases);
      else if (path.getName().endsWith(".class")
          && path.isFile() && isTestCase(path))
        testCases.add(path);
  }

  private boolean isTestCase(File file) throws IOException {
    InputStream stream = new BufferedInputStream(new FileInputStream(file));
    try {
      ClassReader reader = new ClassReader(stream);
      JUnit4ClassVisitor visitor = new JUnit4ClassVisitor();
      reader.accept(visitor, true);
      return visitor.isTestCase;
    } finally {
      stream.close();
    }
  }
}
