package org.spockframework.builder

class XmlConfigurationSource implements IConfigurationSource {
  private final Node xml

  XmlConfigurationSource(Node xml) {
    this.xml = xml
  }

  void configure(IConfigurationTarget target) {
    getNonTextChildren(xml).each { node ->
      target.configureSlot(node.name().toString(), [node.attributes(),
          getTextChildren(node)].findAll { it } as List, new XmlConfigurationSource(node))
    }
  }
  
  private List<String> getTextChildren(Node node) {
    node.children().findAll { it instanceof String }
  }

  private List<Node> getNonTextChildren(Node node) {
    node.children().findAll { !(it instanceof String) }
  }
}
