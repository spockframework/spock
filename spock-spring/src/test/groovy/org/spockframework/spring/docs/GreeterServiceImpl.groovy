package org.spockframework.spring.docs

class GreeterServiceImpl implements GreeterService {
  @Override
  String getGreeting() {
    return 'Hello World'
  }
}
