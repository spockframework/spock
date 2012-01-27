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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.Node;
import groovy.util.XmlParser;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.spockframework.builder.*;
import org.spockframework.util.GroovyRuntimeUtil;
import org.spockframework.util.InternalSpockError;
import org.xml.sax.SAXException;
import spock.config.ConfigurationException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class ModelSourceFactory {
  public IModelSource fromGroovyScript(DelegatingScript script) {
    return new DelegatingScriptModelSource(script);
  }
  
  public IModelSource fromGroovyScript(String script) {
    GroovyShell shell = createShell();
    try {
      DelegatingScript delegatingScript = (DelegatingScript) shell.parse(script);
      return fromGroovyScript(delegatingScript);
    } catch (CompilationFailedException e) {
      throw new ConfigurationException("Error compiling configuration script '%s'", e, script);
    }
  }

  public IModelSource fromGroovyScript(File script) {
    GroovyShell shell = createShell();
    try {
      DelegatingScript delegatingScript = (DelegatingScript) shell.parse(script);
      return fromGroovyScript(delegatingScript);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration script '%s'", e, script);
    } catch (CompilationFailedException e) {
      throw new ConfigurationException("Error compiling configuration script '%s'", e, script);
    }
  }


  public IModelSource fromGroovyScript(Reader script) {
    GroovyShell shell = createShell();
    try {
      DelegatingScript delegatingScript = (DelegatingScript) shell.parse(script);
      return fromGroovyScript(delegatingScript);
    } catch (CompilationFailedException e) {
      throw new ConfigurationException("Error compiling configuration script '%s'", e, script);
    } catch (RuntimeException e) {
      if (e.getMessage().startsWith("Impossible to read")) {
        throw new ConfigurationException("Error reading configuration script '%s'", e.getCause(), script);
      }
      throw e;
    } 
  }

  public IModelSource fromProperties(Map<String, ?> properties) {
    return new PropertiesModelSource(properties);
  }

  @SuppressWarnings("unchecked")
  public IModelSource fromProperties(File properties) throws IOException {
    Properties props = new Properties();
    props.load(new InputStreamReader(new FileInputStream(properties), "utf-8"));
    return fromProperties((Map) props);
  }

  @SuppressWarnings("unchecked")
  public IModelSource fromProperties(Reader properties) throws IOException {
    Properties props = new Properties();
    props.load(properties);
    return fromProperties((Map) props);
  }

  public IModelSource fromSystemProperties() {
    Map<String, String> properties = new HashMap<String, String>();
    
    @SuppressWarnings("unchecked")
    Map<String, String> systemProperties = (Map) System.getProperties();

    for (Map.Entry<String, String> systemProperty: systemProperties.entrySet()) {
      if (systemProperty.getKey().startsWith("spock.")) {
        properties.put(systemProperty.getKey().substring(6), systemProperty.getValue());
      }
    }

    return fromProperties(properties);
  }
  
  public IModelSource fromJson(String json) {
    throw new UnsupportedOperationException("JSON configuration is only supported in Spock for Groovy 1.8 and higher");
  }

  public IModelSource fromJson(File json) {
    throw new UnsupportedOperationException("JSON configuration is only supported in Spock for Groovy 1.8 and higher");
  }

  public IModelSource fromJson(Reader json) {
    throw new UnsupportedOperationException("JSON configuration is only supported in Spock for Groovy 1.8 and higher");
  }

  public IModelSource fromXml(Node xml) {
    try {
      // don't want no joint compilation
      Class<?> clazz = getClass().getClassLoader().loadClass("org.spockframework.builder.XmlModelSource");
      return (IModelSource) GroovyRuntimeUtil.invokeConstructor(clazz, xml);
    } catch (ClassNotFoundException e) {
      throw new InternalSpockError("Error parsing configuration script '%s'", e).withArgs(xml);
    }
  }

  public IModelSource fromXml(String xml) {
    try {
      // XmlSlurper can't parse text nodes, hence we use XmlParser
      XmlParser parser = new XmlParser();
      return fromXml(parser.parseText(xml));
    } catch (ParserConfigurationException e) {
      throw new InternalSpockError("Error parsing configuration script '%s'", e).withArgs(xml);
    } catch (SAXException e) {
      throw new ConfigurationException("Error parsing configuration script '%s'", e, xml);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration script '%s'", e, xml);
    }
  }

  public IModelSource fromXml(File xml) {
    try {
      // XmlSlurper can't parse text nodes, hence we use XmlParser
      XmlParser parser = new XmlParser();
      return fromXml(parser.parse(xml));
    } catch (ParserConfigurationException e) {
      throw new InternalSpockError("Error parsing configuration script '%s'", e).withArgs(xml);
    } catch (SAXException e) {
      throw new ConfigurationException("Error parsing configuration script '%s'", e, xml);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration script '%s'", e, xml);
    }
  }

  public IModelSource fromXml(Reader xml) {
    try {
      // XmlSlurper can't parse text nodes, hence we use XmlParser
      XmlParser parser = new XmlParser();
      return fromXml(parser.parse(xml));
    } catch (ParserConfigurationException e) {
      throw new InternalSpockError("Error parsing configuration script '%s'", e).withArgs(xml);
    } catch (SAXException e) {
      throw new ConfigurationException("Error parsing configuration script '%s'", e, xml);
    } catch (IOException e) {
      throw new ConfigurationException("Error reading configuration script '%s'", e, xml);
    }
  }

  private GroovyShell createShell() {
    CompilerConfiguration compilerSettings = new CompilerConfiguration();
    compilerSettings.setScriptBaseClass(DelegatingScript.class.getName());
    return new GroovyShell(getClass().getClassLoader(), new Binding(), compilerSettings);
  }
}
