package org.spockframework.buildsupport.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.spockframework.buildsupport.SpeckClassFileFinder;

/**
 * Finds all test classes that can be run with JUnit 4 (including but not
 * limited to Spock specifications), and configures Surefire accordingly.
 *
 * @author Peter Niederwieser
 * @goal find-specks
 * @phase process-test-classes
 */
public class FindSpecksMojo extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   */
  MavenProject project;
  
  /**
   * @parameter expression="${project.build.testOutputDirectory}"
   */
  File testOutputDirectory;

  public void execute() throws MojoExecutionException {
    if (!testOutputDirectory.exists()) {
      getLog().info(String.format("Found 0 Spock specifications"));
      return;
    }

    List<File> tests;
    try {
      tests = new SpeckClassFileFinder().findSpecks(testOutputDirectory);
    } catch (IOException e) {
      // chaining the exception would result in a cluttered error message
      throw new MojoExecutionException(e.toString());
    }

    getLog().info(String.format("Found %d Spock specifications", tests.size()));
    
    Plugin plugin = getSurefirePlugin();
    Xpp3Dom config = (Xpp3Dom)plugin.getConfiguration();
    if (config == null) {
      config = new Xpp3Dom("configuration");
      plugin.setConfiguration(config);
    }
    
    Xpp3Dom includes = config.getChild("includes");
    if (includes == null) {
      includes = new Xpp3Dom("includes");
      config.addChild(includes);
    }

    for (File test : tests) {
      String relativePath = test.getAbsolutePath().substring(testOutputDirectory.getAbsolutePath().length() + 1);
      String relativePathJavaExt = relativePath.substring(0, relativePath.length() - ".class".length()) + ".java";
      Xpp3Dom includeNode = new Xpp3Dom("include");
      includeNode.setValue(relativePathJavaExt);
      includes.addChild(includeNode);
    }
  }

  private void checkTestOutputDirectoryExists() {


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
}
