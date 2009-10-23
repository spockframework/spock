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
 * Finds all class files below a base directory that contain a Speck.
 *
 * @author Peter Niederwieser
 */
public class SpeckClassFileFinder {
  public List<File> findSpecks(File baseDir) throws IOException {
    if (!baseDir.isDirectory())
      throw new FileNotFoundException(String.format("directory %s not found", baseDir));

    List<File> specks = new ArrayList<File>();
    doFindSpecks(baseDir, specks);
    Collections.sort(specks);
    return specks;
  }

  private void doFindSpecks(File dir, List<File> foundSpecks) throws IOException {
    for (File path: dir.listFiles())
      if (path.isDirectory())
        doFindSpecks(path, foundSpecks);
      else if (isSpeck(path))
        foundSpecks.add(path);
  }

  public boolean isSpeck(File file) throws IOException {
    if (!(file.getName().endsWith(".class") && file.isFile()))
      return false;
    
    InputStream stream = new BufferedInputStream(new FileInputStream(file));
    try {
      ClassReader reader = new ClassReader(stream);
      SpeckClassFileVisitor visitor = new SpeckClassFileVisitor();
      reader.accept(visitor, true);
      return visitor.isSpeck;
    } finally {
      stream.close();
    }
  }
}
