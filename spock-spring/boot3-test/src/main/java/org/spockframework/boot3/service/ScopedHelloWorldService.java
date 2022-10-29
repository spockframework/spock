package org.spockframework.boot3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class ScopedHelloWorldService {

  @Value("${name:World Scope!}")
  private String name;

  public String getHelloMessage() {
    return "Hello " + this.name;
  }

}
