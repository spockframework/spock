package org.spockframework.boot.web;

import org.spockframework.boot.service.HelloWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring-MVC controller class.
 */
@RestController
public class HelloWorldController {

  private HelloWorldService service;

  @Autowired
  public HelloWorldController(HelloWorldService service) {
    this.service = service;
  }

  @RequestMapping("/")
  public String hello() {
    return service.getHelloMessage();
  }

}
