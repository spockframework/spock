/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessControlException;

import groovy.lang.*;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import org.spockframework.builder.DelegatingScript;
import org.spockframework.util.Nullable;

import spock.config.ConfigurationException;

public class ConfigurationScriptLoader {
  private final String configPropertyKey;
  private final String classPathLocation;
  private final String fileSystemLocation;

  public ConfigurationScriptLoader(File spockUserHome) {
    this("spock.configuration", "SpockConfig.groovy", spockUserHome + "/SpockConfig.groovy");
  }

  /**
   * For testing purposes. Do not use directly.
   */
  ConfigurationScriptLoader(String configPropertyKey, String classPathLocation, String fileSystemLocation) {
    this.configPropertyKey = configPropertyKey;
    this.classPathLocation = classPathLocation;
    this.fileSystemLocation = fileSystemLocation;
  }

  public @Nullable DelegatingScript loadAutoDetectedScript() {
    DelegatingScript script = loadScriptFromSystemPropertyInducedLocation(configPropertyKey);
    if (script != null) return script;

    script = loadScriptFromClassPathLocation(classPathLocation);
    if (script != null) return script;

    script = loadScriptFromFileSystemLocation(fileSystemLocation);
    if (script != null) return script;

    return null;
  }

  public DelegatingScript loadClosureBasedScript(final Closure<?> closure) {
    return new DelegatingScript() {
      @Override
      public Object run() {
        GroovyRuntimeUtil.invokeClosure(closure);
        return null;
      }

      @Override
      public void $setDelegate(Object delegate) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(delegate);
      }
    };
  }

  private @Nullable DelegatingScript loadScriptFromSystemPropertyInducedLocation(String propertyKey) {
    String location = System.getProperty(propertyKey);
    if (location == null || location.length() == 0) return null;

    DelegatingScript script = loadScriptFromClassPathLocation(location);
    if (script != null) return script;

    script = loadScriptFromFileSystemLocation(location);
    if (script != null) return script;

    throw new ConfigurationException("Cannot find configuration script '%s'", location);
  }

  private @Nullable DelegatingScript loadScriptFromFileSystemLocation(String location) {
    File file = new File(location);

    try {
      if (!file.exists()) return null;
    } catch (AccessControlException e) {
      // no permission to check for existence of file (e.g. on Spock Web Console),
      // so let's just assume it's not there and continue
      return null;
    }

    GroovyShell shell = createShell();

    try {
      return (DelegatingScript) shell.parse(file);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration script '%s'", location);
    } catch (CompilationFailedException e) {
      throw new ConfigurationException("Error compiling configuration script '%s'", location);
    }
  }

  private @Nullable DelegatingScript loadScriptFromClassPathLocation(String location) {
    URL url = this.getClass().getClassLoader().getResource(location);
    if (url == null) return null;

    GroovyShell shell = createShell();
    GroovyCodeSource codeSource = null;
    // try block below for compatibility pre/post Groovy 2.4.7 (see GROOVY-8126)
    try {
      codeSource = new GroovyCodeSource(url);
    } catch (Exception e) {
      throw new ConfigurationException("Error reading configuration script '%s'", location);
    }
    try {
      return (DelegatingScript) shell.parse(codeSource);
    } catch (CompilationFailedException e) {
      throw new ConfigurationException("Error compiling configuration script '%s'", location);
    }
  }

  private GroovyShell createShell() {
    CompilerConfiguration compilerSettings = new CompilerConfiguration();
    compilerSettings.setScriptBaseClass(DelegatingScript.class.getName());
    return new GroovyShell(getClass().getClassLoader(), new Binding(), compilerSettings);
  }
}

