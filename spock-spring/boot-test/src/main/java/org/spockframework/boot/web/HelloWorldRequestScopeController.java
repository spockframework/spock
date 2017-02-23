package org.spockframework.boot.web;

import org.spockframework.boot.service.HelloWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring-MVC controller class.
 */
@Scope("request")
@RestController
public class HelloWorldRequestScopeController {

  private HelloWorldService service;

  @Autowired
  public HelloWorldRequestScopeController(HelloWorldService service) {
    this.service = service;
  }

  @RequestMapping("/hello-from-request-scope")
  public String hello() {
    return service.getHelloMessage();
  }

}
