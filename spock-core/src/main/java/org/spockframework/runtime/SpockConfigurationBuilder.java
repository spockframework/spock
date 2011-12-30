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

import java.io.*;
import java.util.*;

import groovy.util.XmlParser;
import groovy.util.XmlSlurper;
import org.spockframework.builder.*;

public class SpockConfigurationBuilder {
  private final ITypeCoercer coercer = createCoercer();
  private final ISlotFinder slotFinder = createSlotFinder();
  private final List<IConfigurationSource> sources = new ArrayList<IConfigurationSource>();

  public SpockConfigurationBuilder fromGroovyScript(DelegatingScript script) {
    sources.add(new DelegatingScriptConfigurationSource(script));
    return this;
  }
  
  public SpockConfigurationBuilder fromGroovyScript(String script) {
    return this; // TODO
  }

  public SpockConfigurationBuilder fromGroovyScript(File script) {
    return this; // TODO
  }

  public SpockConfigurationBuilder fromGroovyScript(InputStream script) {
    return this; // TODO
  }
  
  public SpockConfigurationBuilder fromProperties(Map<String, ?> properties) {
    sources.add(new PropertiesConfigurationSource(properties));
    return this;
  }

  @SuppressWarnings("unchecked")
  public SpockConfigurationBuilder fromProperties(File properties) throws IOException {
    Properties props = new Properties();
    props.load(new InputStreamReader(new FileInputStream(properties), "utf-8"));
    return fromProperties((Map) props);
  }

  public SpockConfigurationBuilder fromSystemProperties() {
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
  
  public SpockConfigurationBuilder fromXml(String xml) throws Exception {
    // apparently XmlSlurper can't parse text nodes
    XmlParser parser = new XmlParser();
    sources.add(new XmlConfigurationSource(parser.parseText(xml)));
    return this;
  }
  
  public SpockConfigurationBuilder fromXml(File xml) {
    return this; // TODO
  }
  
  public SpockConfigurationBuilder fromXml(InputStream xml) {
    return this; // TODO
  }

  // IDEA: move this into a SpockConfiguration class s.t. SpockConfigurationBuilder becomes a "real" builder
  public void build(List<Object> configurationBlocks) {
    IConfigurationTarget target = new SpockConfigurationTarget(configurationBlocks, coercer);
    for (IConfigurationSource source: sources) {
      source.configure(target);
    }
  }

  private ITypeCoercer createCoercer() {
    CoercerChain chain = new CoercerChain();
    chain.add(new StringCoercer(chain));
    chain.add(new ClosureConfigurationSourceCoercer());
    chain.add(new GroovyCoercer());
    return chain;
  }

  private ISlotFinder createSlotFinder() {
    return new SlotFinderChain(Arrays.asList(new PropertySlotFinder(), new AddSlotFinder(), new CollectionSlotFinder()));
  }
}
