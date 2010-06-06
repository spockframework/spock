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

package org.spockframework.buildsupport.maven;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.spockframework.buildsupport.SpecClassFileFinder;
import org.spockframework.runtime.OptimizeRunOrderSuite;
import org.spockframework.util.IoUtil;
import org.spockframework.util.TextUtil;

/**
 * Finds all Spock specifications in a Maven project, and adds them as
 * &lt;include&gt;'s to Surefire's configuration. Any other explicit &lt;include&gt;'s
 * remain intact; however, Surefire's default includes
 * (&#42;&#42;/Test&#42;.java &#42;&#42;/&#42;Test.java &#42;&#42;/&#42;TestCase.java)
 * will no longer take effect (add them manually if required).
 *
 * @author Peter Niederwieser
 * @goal find-specs
 * @phase process-test-classes
 */
public class FindSpecsMojo extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   */
  private MavenProject project;

  /**
   * @parameter expression="${project.build.testOutputDirectory}"
   */
  private File testOutputDirectory;

  /**
   * @parameter default="false"
   */
  private boolean skip;

  /**
   * @parameter default="false"
   */
  private boolean optimizeRunOrder;

  public void execute() throws MojoExecutionException {
    if (!shouldRun()) return;
    List<String> specNames = findSpecs();
    addToSurefireConfiguration(specNames);
  }

  private boolean shouldRun() {
    if (skip) return false;

    if (!testOutputDirectory.exists()) {
      getLog().info(String.format("Found 0 Spock specifications"));
      return false;
    }

    return true;
  }

  private List<String> findSpecs() throws MojoExecutionException {
    final List<String> specNames;

    try {
      List<File> specFiles = new SpecClassFileFinder().findRunnableSpecs(testOutputDirectory);
      specNames = new ArrayList<String>(specFiles.size());
      for (File file : specFiles) {
        String path = file.getAbsolutePath();
        String name = path.substring(testOutputDirectory.getAbsolutePath().length() + 1,
            path.length() - ".class".length()).replace(File.separatorChar, '.');
        specNames.add(name);
      }
    } catch (IOException e) {
      // chaining the exception would result in a cluttered error message
      throw new MojoExecutionException(e.toString());
    }

    getLog().info(String.format("Found %d Spock specifications", specNames.size()));
    return specNames;
  }

  private void addToSurefireConfiguration(List<String> specNames) throws MojoExecutionException {
    Plugin plugin = getSurefirePlugin();

    Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
    if (config == null) {
      config = new Xpp3Dom("configuration");
      plugin.setConfiguration(config);
    }

    Xpp3Dom includes = config.getChild("includes");
    if (includes == null) {
      includes = new Xpp3Dom("includes");
      config.addChild(includes);
    }

    Xpp3Dom systemProperties = config.getChild("systemPropertyVariables");
    if (systemProperties == null) {
      systemProperties = new Xpp3Dom("systemPropertyVariables");
      config.addChild(systemProperties);
    }

    if (optimizeRunOrder) {
      getLog().info(String.format("Optimizing spec run order"));
      copySuiteClassToTestOutputDirectory();
      addSystemProperty(systemProperties, OptimizeRunOrderSuite.CLASSES_TO_RUN_KEY, TextUtil.join(",", specNames));
      addInclude(includes, OptimizeRunOrderSuite.class.getName());
    } else {
      for (String name : specNames)
        addInclude(includes, name);
    }
  }

  private Plugin getSurefirePlugin() throws MojoExecutionException {
    @SuppressWarnings("unchecked")
    List<Plugin> plugins = project.getBuildPlugins();

    for (Plugin plugin : plugins)
      if (plugin.getGroupId().equals("org.apache.maven.plugins")
          && plugin.getArtifactId().equals("maven-surefire-plugin"))
        return plugin;

    throw new MojoExecutionException("Surefire plugin not found; make sure it is bound to a lifecycle phase");
  }

  private void copySuiteClassToTestOutputDirectory() throws MojoExecutionException {
    try {
      InputStream source =
          getClass().getClassLoader().getResourceAsStream(
              OptimizeRunOrderSuite.class.getName().replace(".", "/") + ".class");
      OutputStream target = new FileOutputStream(new File(testOutputDirectory,
          OptimizeRunOrderSuite.class.getName().replace('.', File.separatorChar) + ".class"));
      IoUtil.copyStream(source, target);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to copy OptimizeRunOrderSuite.class to test-classes directory\n"
          + e.toString());
    }
  }

  private void addSystemProperty(Xpp3Dom systemProperties, String key, String value) {
    Xpp3Dom propertyNode = new Xpp3Dom(key);
    propertyNode.setValue(value);
    systemProperties.addChild(propertyNode);
  }

  private void addInclude(Xpp3Dom includes, String name) {
    Xpp3Dom includeNode = new Xpp3Dom("include");
    includeNode.setValue(name.replace('.', '/') + ".java");
    includes.addChild(includeNode);
  }
}
