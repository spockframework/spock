/**
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.buildsupport;

import java.io.*;
import java.util.*;

import org.objectweb.asm.ClassReader;

/**
 * Finds all class files below a base directory that contain a runnable spec.
 *
 * @author Peter Niederwieser
 */
public class SpecClassFileFinder {
  public List<File> findRunnableSpecs(File baseDir) throws IOException {
    if (!baseDir.isDirectory())
      throw new FileNotFoundException(String.format("directory %s not found", baseDir));

    List<File> specs = new ArrayList<File>();
    doFindRunnableSpecs(baseDir, specs);
    Collections.sort(specs);
    return specs;
  }

  private void doFindRunnableSpecs(File dir, List<File> foundSpecs) throws IOException {
    for (File path: dir.listFiles())
      if (path.isDirectory())
        doFindRunnableSpecs(path, foundSpecs);
      else if (isRunnableSpec(path))
        foundSpecs.add(path);
  }

  public boolean isRunnableSpec(File file) throws IOException {
    if (!(file.getName().endsWith(".class") && file.isFile()))
      return false;
    
    InputStream stream = new BufferedInputStream(new FileInputStream(file));
    try {
      ClassReader reader = new ClassReader(stream);
      SpecClassFileVisitor visitor = new SpecClassFileVisitor();
      reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      return visitor.isRunnableSpec();
    } finally {
      stream.close();
    }
  }
}
