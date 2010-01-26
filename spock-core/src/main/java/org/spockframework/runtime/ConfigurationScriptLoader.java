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

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import spock.builder.DelegatingScript;
import spock.config.ConfigurationException;

import groovy.lang.*;

public class ConfigurationScriptLoader {
  private static final String DEFAULT_CONFIGURATION_PATH =
      System.getProperty("user.home") + File.separator + ".spock" + File.separator + "SpockConfig.groovy";

  public DelegatingScript loadScriptFromConfiguredLocation() {
    File file = findConfigurationFile();
    return file == null ? null : loadScript(file);
  }

  public DelegatingScript loadScript(File file) {
    CompilerConfiguration compilerSettings = new CompilerConfiguration();
    compilerSettings.setScriptBaseClass(DelegatingScript.class.getName());
    GroovyShell shell = new GroovyShell(getClass().getClassLoader(), new Binding(), compilerSettings);
    try {
      return (DelegatingScript) shell.parse(file);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration file");  
    } catch (CompilationFailedException e) {
      throw new ConfigurationException("Error compiling configuration script", e);
    }
  }

  public DelegatingScript loadScript(final Closure closure) {
    return new DelegatingScript() {
      @Override
      public Object run() {
        closure.call();
        return null;
      }

      @Override
      public void $setDelegate(Object delegate) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(delegate);
      }
    };
  }

  private File findConfigurationFile() {
    String path = System.getProperty("spock.configuration");
    if (path == null) {
      path = DEFAULT_CONFIGURATION_PATH;
      if (!new File(path).isFile()) return null;
    } else {
      if (!new File(path).isFile())
        throw new ConfigurationException("Cannot find configuration file '%s'", path);
    }
    return new File(path);
  }
}

