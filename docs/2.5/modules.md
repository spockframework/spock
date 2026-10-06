# Modules

## JUnit 4 Module

Integration with JUnit 4 features for Spock 2+ (which internally uses JUnit Platform - part of JUnit 5). Please add dependency [`org.spockframework:spock-junit4`](https://search.maven.org/artifact/org.spockframework/spock-junit4) to your project.


The module is required for:


- running JUnit 4 rules and class ruless (`@Rule`/`@ClassRule`)
- using JUnit 4’s test fixture annotations (`@BeforeClass`, `@Before`, `@After`, `@AfterClass`)


> [!NOTE]
> This module does its best to support old features from JUnit 4, however, users are encouraged to migrate to the native Spock counterparts.


## Guice Module

Integration with the [Guice](https://github.com/google/guice) IoC container. Please add dependency [`org.spockframework:spock-guice`](https://search.maven.org/artifact/org.spockframework/spock-guice) to your project. For examples see the specs in the
[codebase](https://github.com/spockframework/spock/tree/master/spock-guice/src/test/groovy/org/spockframework/guice).


With Spock 1.2+ detached mocks are automatically attached to the `Specification` if they are injected via `@Inject`.


## Spring Module

The Spring module enables integration with [Spring TestContext Framework](https://docs.spring.io/spring/docs/4.1.5.RELEASE/spring-framework-reference/html/testing.html#testcontext-framework).
It supports the following spring annotations `@ContextConfiguration` and `@ContextHierarchy`. Furthermore, it supports the meta-annotation `@BootstrapWith` and so any annotation that is annotated with `@BootstrapWith` will also work, such as `@SpringBootTest`, `@WebMvcTest`. Please add dependency [`org.spockframework:spock-spring`](https://search.maven.org/artifact/org.spockframework/spock-spring) to your project.


### Mocks

Spock 1.1 introduced the `DetachedMockFactory` and the `SpockMockFactoryBean` which allow the creation of Spock mocks outside of a specification.


> [!NOTE]
> Although the mocks can be created outside of a specification, they only work properly inside the scope of a specification.
>       All interactions with them until they are attached to one, are handled by the default behavior and not recorded.
>      
> 
>       Furthermore, mocks can only be attached to one `Specification` instance at a time so keep that in mind when using multi-threaded executions


#### Java Config

```groovy
class DetachedJavaConfig {
  def mockFactory = new DetachedMockFactory()

  @Bean
  GreeterService serviceMock() {
    return mockFactory.Mock(GreeterService)
  }

  @Bean
  GreeterService serviceStub() {
    return mockFactory.Stub(GreeterService)
  }

  @Bean
  GreeterService serviceSpy() {
    return mockFactory.Spy(GreeterServiceImpl)
  }

  @Bean
  FactoryBean<GreeterService> alternativeMock() {
    return new SpockMockFactoryBean(GreeterService)
  }
}
```


#### XML

Spock has spring namespace support, so if you declare the spock namespace with `xmlns:spock="https://www.spockframework.org/spring"` you get access to the convenience functions for creating mocks.


```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:spock="http://www.spockframework.org/spring"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.spockframework.org/spring https://www.spockframework.org/spring/spock.xsd">

  <spock:mock id="serviceMock" class="org.spockframework.spring.docs.GreeterService"/>   <!--1-->
  <spock:stub id="serviceStub" class="org.spockframework.spring.docs.GreeterService"/>   <!--2-->
  <spock:spy id="serviceSpy" class="org.spockframework.spring.docs.GreeterServiceImpl"/> <!--3-->

  <bean id="someExistingBean" class="java.util.ArrayList"/>                              <!--4-->
  <spock:wrapWithSpy ref="someExistingBean"/>                                            <!--4-->

  <bean id="alternativeMock" class="org.spockframework.spring.xml.SpockMockFactoryBean"> <!--5-->
    <constructor-arg value="org.spockframework.spring.docs.GreeterService"/>
    <property name="mockNature" value="MOCK"/>                                           <!--6-->
  </bean>


</beans>
```


1. Creates a `Mock`
2. Creates a `Stub`
3. Creates a `Spy`
4. Wraps an existing bean with a `Spy`. Fails fast if referenced bean is not found.
5. If you don’t want to use the special namespace support you can create the beans via the `SpockMockFactoryBean`
6. The `mockNature` can be `MOCK`, `STUB`, or `SPY` and defaults to `MOCK` if not declared.


#### Usage

To use the mocks just inject them like any other bean and configure them as usual.


```groovy
@Autowired @Named('serviceMock')
GreeterService serviceMock

@Autowired @Named('serviceStub')
GreeterService serviceStub

@Autowired @Named('serviceSpy')
GreeterService serviceSpy

@Autowired @Named('alternativeMock')
GreeterService alternativeMock

def "mock service"() {
  when:
  def result = serviceMock.greeting

  then:
  result == 'mock me'
  1 * serviceMock.getGreeting() >> 'mock me'
}

def "sub service"() {
  given:
  serviceStub.getGreeting() >> 'stub me'

  expect:
  serviceStub.greeting == 'stub me'
}

def "spy service"() {
  when:
  def result = serviceSpy.greeting

  then:
  result == 'Hello World'
  1 * serviceSpy.getGreeting()
}

def "alternative mock service"() {
  when:
  def result = alternativeMock.greeting

  then:
  result == 'mock me'
  1 * alternativeMock.getGreeting() >> 'mock me'
}
```


#### Annotation driven

Spock 1.2 adds support for exporting mocks from a `Specification` into an `ApplicationContext`. This was inspired by
Spring Boot’s `@MockBean`(realised via Mockito) but adapted to fit into Spock style. It does not require any Spring Boot dependencies,
however it requires Spring Framework 4.3.5 or greater to work.


##### Using `@SpringBean`

Registers mock/stub/spy as a spring bean in the test context.


To use `@SpringBean` you have to use a strongly typed field `def` or `Object` won’t work. You also need to directly assign the
`Mock`/`Stub`/`Spy` to the field using the standard Spock syntax. You can even use the initializer blocks to define common behavior,
however they are only picked up once they are attached to the `Specification`.


`@SpringBean` definitions can replace existing Beans in your `ApplicationContext`.


> [!NOTE]
> Spock’s `@SpringBean` actually creates a proxy in the `ApplicationContext` which forwards everything to the current
>       mock instance. The type of the proxy is determined by the type of the annotated field.
>      
> 
>       The proxy attaches itself to the current mock in the setup phase, that is why the mock must be created when the field is initialized.


```groovy
@SpringBean
Service1 service1 = Mock()

@SpringBean
Service2 service2 = Stub() {
  generateQuickBrownFox() >> "blubb"
}

def "injection with stubbing works"() {
  expect:
  service2.generateQuickBrownFox() == "blubb"
}

def "mocking works was well"() {
  when:
  def result = service1.generateString()

  then:
  result == "Foo"
  1 * service1.generateString() >> "Foo"
}
```


> [!CAUTION]
> As with Spring’s own `@MockBean` this will modify your `ApplicationContext`, and will create an unique context for your
>          `Specification` preventing it from being reused by Spring’s
>          [Context Caching](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-ctx-management-caching)
>          outside of the current `Specification`.
>         
> 
>          If you are using a small context this won’t matter much, but if it is a heavy context you might want to use
>          the other approaches, e.g., using the `DetachedMockFactory`.


##### Using `@SpringSpy`

If you want to spy on an existing bean, you can use the `@SpringSpy` annotation to wrap the bean in a spy.
As with `@SpringBean` the field must be of the type you want to spy on, however you cannot use an initializer.


```groovy
@SpringSpy
Service2 service2

@Autowired
Service1 service1

def "default implementation is used"() {
  expect:
  service1.generateString() == "The quick brown fox jumps over the lazy dog."
}

def "mocking works was well"() {
  when:
  def result = service1.generateString()

  then:
  result == "Foo"
  1 * service2.generateQuickBrownFox() >> "Foo"
}
```


##### Using `@StubBeans`

`@StubBeans` registers plain `Stub` instances in an `ApplicationContext`.
Use this if you just need to satisfy some dependencies without actually doing anything with these stubs.
If you need to control the stubs, e.g., configure return values then use `@SpringBean` instead.
Like `@SpringBean` `@StubBeans` also replaced existing BeanDefinitions,so you can use it to remove real beans from an ApplicationContext.
`@StubBeans` can be replaced by `@SpringBean`, this can be useful if you need to replace some `@StubBeans` defined in a parent class.


```groovy
@StubBeans(Service2)
@ContextConfiguration(classes = DemoMockContext)
class StubBeansExamples extends Specification {
```


#### Spring Boot

The recommended way to use Spock mocks in `@WebMvcTest` or other `@SpringBootTest`-style tests,
is to use the `@SpringBean` and `@SpringSpy` annotations as shown above.


Alternatively you can use an embedded config annotated with `@TestConfiguration` and to create the mocks using the `DetachedMockFactory`.


```groovy
@WebMvcTest
class WebMvcTestIntegrationSpec extends Specification {

  @Autowired
  MockMvc mvc

  @Autowired
  HelloWorldService helloWorldService

  def "spring context loads for web mvc slice"() {
    given:
    helloWorldService.getHelloMessage() >> 'hello world'

    expect: "controller is available"
    mvc.perform(MockMvcRequestBuilders.get("/"))
      .andExpect(status().isOk())
      .andExpect(content().string("hello world"))
  }

  @TestConfiguration
  static class MockConfig {
    def detachedMockFactory = new DetachedMockFactory()

    @Bean
    HelloWorldService helloWorldService() {
      return detachedMockFactory.Stub(HelloWorldService)
    }
  }
}
```


For more examples see the specs in the [codebase](https://github.com/spockframework/spock/tree/master/spock-spring/src/test/groovy/org/spockframework/spring) and [boot examples](https://github.com/spockframework/spock/tree/master/spock-spring/boot-test/src/test/groovy/org/spockframework/boot).


### Scopes

Spock ignores bean that is not a `singleton` (in the `singleton` scope) by default. To enable mocks to work for scoped beans
you need to add `@ScanScopedBeans` to the spec and make sure that the scope allows access to the bean during the setup phase.


> [!NOTE]
> The `request` and `session` scope will throw exceptions by default, if there is no active request/session.


You can limit the scanning to certain scopes by using the `value` property of `@ScanScopedBeans`.


### Shared fields injection

Due to certain limitations, injection into shared fields is not enabled by default but can be opted-in to.
Refer to javadoc of `org.spockframework.spring.EnableSharedInjection` for further information.


## Tapestry Module

Integration with the [Tapestry5](https://tapestry.apache.org/tapestry5/) IoC container. Please add dependency [`org.spockframework:spock-tapestry`](https://search.maven.org/artifact/org.spockframework/spock-tapestry) to your project. For examples see the specs in the
[codebase](https://github.com/spockframework/spock/tree/master/spock-tapestry/src/test/groovy/org/spockframework/tapestry).


## Unitils Module

Integration with the [Unitils](https://www.unitils.org/) library. Please add dependency [`org.spockframework:spock-unitils`](https://search.maven.org/artifact/org.spockframework/spock-unitils) to your project. For examples see the specs in the
[codebase](https://github.com/spockframework/spock/tree/master/spock-unitils/src/test/groovy/org/spockframework/unitils).


## Grails Module

The Grails plugin has moved to its own [GitHub project](https://github.com/spockframework/spock-grails). It has legacy status and was last released for [Spock 0.7 and Groovy versions 1.8 and 2.0](https://search.maven.org/artifact/org.spockframework/spock-grails), because it is no longer necessary.


> [!NOTE]
> Grails 2.3 and higher have built-in Spock support and do not require a plugin.

