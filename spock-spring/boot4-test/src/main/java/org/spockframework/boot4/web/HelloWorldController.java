package org.spockframework.boot4.web;

import org.spockframework.boot4.service.HelloWorldService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
