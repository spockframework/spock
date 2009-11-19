/*
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

package org.spockframework.buildsupport.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;

import org.spockframework.buildsupport.SpecClassFileFinder;

/**
 * Ant selector that selects class files containing Spock specifications.
 */
public class SpecClassFileSelector extends BaseExtendSelector {
  private final SpecClassFileFinder finder = new SpecClassFileFinder();

  @Override
  public boolean isSelected(File baseDir, String filename, File file) {
    try {
      return finder.isSpec(file);
    } catch (IOException e) {
      String msg = e.getMessage();
      log("Error reading class file '" + filename + (msg == null ? "'" : "': " + msg), Project.MSG_WARN);
      return false;
    }
  }
}
