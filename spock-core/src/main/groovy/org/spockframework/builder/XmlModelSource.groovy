/*
 * Copyright 2011 the original author or authors.
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

package org.spockframework.builder

class XmlModelSource implements IModelSource {
  private final Node xml

  XmlModelSource(Node xml) {
    this.xml = xml
  }

  void configure(IModelTarget target) {
    getNonTextChildren(xml).each { node ->
      target.configureSlot(node.name().toString(), getValues(node), new XmlModelSource(node))
    }
  }
  
  private List<String> getTextChildren(Node node) {
    node.children().findAll { it instanceof String }
  }

  private List<Node> getNonTextChildren(Node node) {
    node.children().findAll { !(it instanceof String) }
  }
  
  private List<Object> getValues(Node node) {
    def result = []
    if (!node.attributes().isEmpty()) {
      result << node.attributes()
    }
    result.addAll(getTextChildren(node))
    result
  }
}
