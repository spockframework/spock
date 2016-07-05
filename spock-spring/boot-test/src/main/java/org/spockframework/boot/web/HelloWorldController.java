package org.spockframework.boot.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring-MVC controller class.
 */
@RestController
public class HelloWorldController {

  @RequestMapping("/")
  public String hello() {
    return "hello world";
  }

}
