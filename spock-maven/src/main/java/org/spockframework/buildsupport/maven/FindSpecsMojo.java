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

import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.spockframework.buildsupport.SpecClassFileFinder;
import org.spockframework.runtime.OptimizeRunOrderSuite;
import org.spockframework.util.IoUtil;
import org.spockframework.util.TextUtil;

/**
 * Plugin that auto-detects and runs all Spock specs in a Maven project.
 * Specs are run by adding them as &lt;include&gt;'s to Surefire's
 * configuration. Unless <tt>overrideSurefireIncludes</tt> is set to <tt>true</tt>,
 * Surefire's default includes or any explicit &lt;include&gt;'s overriding
 * those defaults will remain intact. This means that Spock specs will get run
 * together with all other JUnit-based tests that would get run without this
 * plugin being present.
 *
 * <p><strong>Note:</strong>This plugin is <em>not</em> required for using Spock
 * together with Maven; it merely adds some advanced capabilities like auto-
 * detection of specs. If you decide not to use this plugin, make sure that your
 * spec classes conform to Surefire's naming conventions. By default, these are
 * &#42;&#42;/Test&#42;.java, &#42;&#42;/&#42;Test.java, and &#42;&#42;/&#42;TestCase.java.
 * 
 * @author Peter Niederwieser
 * @goal find-specs
 * @phase process-test-classes
 */
public class FindSpecsMojo extends AbstractMojo {
  /**
   * @component
   */
  private RuntimeInformation runtimeInformation;

  /**
   * @parameter expression="${project}"
   */
  private MavenProject project;

  /**
   * @parameter expression="${project.build.testOutputDirectory}"
   */
  private File testOutputDirectory;

  /**
   * If <tt>true</tt>, the execution of this plugin will be skipped.
   *
   * @parameter expression="${findspecs.skip}" default="false"
   */
  private boolean skip;

  /**
   * If <tt>true</tt>, any existing Surefire &lt;include&gt;'s will
   * be overridden to make sure that only Spock specs will get run.
   *
   * @parameter expression="${findspecs.overrideSurefireIncludes}" default="false"
   */
  private boolean overrideSurefireIncludes;

  /**
   * If non-null, &lt;include&gt;'s for Spock specs will be added to the
   * configuration of the specified Surefire execution. Otherwise, they
   * will be added to the global Surefire configuration.
   *
   * @parameter expression="${findspecs.surefireExecutionId}" default="null"
   */
  private String surefireExecutionId;

  /**
   * If <tt>true</tt> the run order of spec <em>classes</em> will be
   * optimized such that frequently failing specs are run first. Note
   * that this requires all specs to be wrapped with a dynamically
   * generated JUnit suite, which may have a negative impact on test
   * reporting.
   *
   * @parameter expression="${findspecs.optimizeRunOrder}" default="false"
   */
  private boolean optimizeRunOrder;

  public void execute() throws MojoExecutionException {
    checkMavenVersion();

    if (!shouldRun()) return;

    List<String> specNames = findSpecs();
    configureSurefire(specNames);
  }

  private void checkMavenVersion() throws MojoExecutionException {
    if (runtimeInformation.getApplicationVersion().getMajorVersion() == 3) {
      throw new MojoExecutionException("The Spock Maven plugin is not compatible with Maven 3. To use Spock with " +
          "Maven 3, remove this plugin and make sure that your spec classes adhere to Surefire's naming conventions " +
          "(e.g. '*Test'). You can also configure Surefire to support other naming conventions (e.g. '*Spec').");
    }
  }

  private boolean shouldRun() {
    if (skip) {
      getLog().info(String.format("Skipping goal 'find-specs'"));
      return false;
    }

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

  private void configureSurefire(List<String> specNames) throws MojoExecutionException {
    ConfigurationContainer container = getSurefireConfigurationContainer();

    Xpp3Dom config = (Xpp3Dom) container.getConfiguration();
    if (config == null) {
      config = new Xpp3Dom("configuration");
      container.setConfiguration(config);
    }

    Xpp3Dom includes = getOrAddChild(config, "includes");
    if (overrideSurefireIncludes) {
      removeChildren(includes);
    } else if (includes.getChildCount() == 0) {
      addDefaultSurefireIncludes(includes);
    }

    if (optimizeRunOrder) {
      optimizeRunOrder(config, includes, specNames);
    } else {
      for (String name : specNames) addInclude(includes, name);
    }
  }

  private ConfigurationContainer getSurefireConfigurationContainer() throws MojoExecutionException {
    @SuppressWarnings("unchecked")
    List<Plugin> plugins = project.getBuildPlugins();

    for (Plugin plugin : plugins) {
      if (!plugin.getGroupId().equals("org.apache.maven.plugins")) continue;
      if (!plugin.getArtifactId().equals("maven-surefire-plugin")) continue;

      if (surefireExecutionId == null) return plugin;

      PluginExecution execution = (PluginExecution) plugin.getExecutionsAsMap().get(surefireExecutionId);
      if (execution == null) throw new MojoExecutionException(
          String.format("Cannot find Surefire execution with ID '%s'", surefireExecutionId));

      return execution;
    }

    throw new MojoExecutionException("Surefire plugin not found; make sure it is bound to a lifecycle phase");
  }

  private void optimizeRunOrder(Xpp3Dom config, Xpp3Dom includes, List<String> specNames) throws MojoExecutionException {
    getLog().info(String.format("Optimizing spec run order"));
    copySuiteClassToTestOutputDirectory();
    addInclude(includes, OptimizeRunOrderSuite.class.getName());
    Xpp3Dom systemProperties = getOrAddChild(config, "systemPropertyVariables");
    addChild(systemProperties, OptimizeRunOrderSuite.CLASSES_TO_RUN_KEY, TextUtil.join(",", specNames));
  }

  private void addInclude(Xpp3Dom includes, String value) {
    addChild(includes, "include", value.replace('.', '/') + ".java");
  }

  private void addDefaultSurefireIncludes(Xpp3Dom includes) {
    addInclude(includes, "**/Test*");
    addInclude(includes, "**/*Test");
    addInclude(includes, "**/*TestCase");
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

  private Xpp3Dom addChild(Xpp3Dom parent, String name) {
    return addChild(parent, name, null);
  }

  private Xpp3Dom addChild(Xpp3Dom parent, String name, String value) {
    Xpp3Dom child = new Xpp3Dom(name);
    child.setValue(value);
    parent.addChild(child);
    return child;
  }

  private Xpp3Dom getOrAddChild(Xpp3Dom parent, String name) {
    Xpp3Dom child = parent.getChild(name);
    if (child == null) child = addChild(parent, name);
    return child;
  }

  private void removeChildren(Xpp3Dom parent) {
    for (int i = parent.getChildCount() - 1; i >= 0; i--) parent.removeChild(i);
  }
}
