package org.spockframework.runtime

import org.spockframework.runtime.extension.IStore
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.function.Supplier

class NamespacedExtensionStoreSpec extends Specification {
  static final NS1 = IStore.Namespace.create("NS1")
  static final NS2 = IStore.Namespace.create("NS2")

  @AutoCleanup
  def rootStore = StoreProvider.createRootStore()
  @AutoCleanup
  def childStore = rootStore.createChildStoreProvider()

  def ns1Root = rootStore.getStore(NS1)
  def ns1Child = childStore.getStore(NS1)

  def "can put something in and retrieve it"() {
    when:
    ns1Root.put("key", "something")

    then:
    ns1Root.get("key") == "something"
    ns1Root.get("key", String) == "something"
  }

  def "can put something in root and retrieve it in the child"() {
    when:
    ns1Root.put("key", "something")

    then:
    ns1Child.get("key") == "something"
    ns1Child.get("key", String) == "something"
  }

  def "child values shadow parent values"() {
    when:
    ns1Root.put("key", "something")
    ns1Child.put("key", "otherthing")

    then:
    ns1Root.get("key") == "something"
    ns1Child.get("key", String) == "otherthing"

    when:
    ns1Child.remove("key")

    then:
    ns1Child.get("key") == "something"
  }

  def "stores with different namespaces don't leak into each other"() {
    given:
    ns1Root.put("key", "something")

    and: "another store"
    def ns2Store = rootStore.getStore(NS2)

    and: "another store with an appended namespace"
    def ns1Sub = rootStore.getStore(NS1.append("sub"))

    expect:
    ns2Store.get("key") == null
    ns1Sub.get("key") == null
  }


  def "returns default value if absent"() {
    expect:
    ns1Root.getOrDefault("key", String, "default") == "default"

    and: "value ist still missing"
    ns1Root.get("key") == null
  }

  def "returns default value from supplier if absent"() {
    expect:
    // explicit cast is necessary as groovy dynamic dispatch would otherwise treat the closure as default value 🤦
    ns1Root.getOrDefault("key", String, { "default" } as Supplier<String>) == "default"

    and: "value ist still missing"
    ns1Root.get("key") == null
  }

  def "default value supplier is not called if value is present"() {
    given:
    ns1Root.put("key", "something")

    expect:
    ns1Root.getOrDefault("key", String, { shouldNotBeCalled() } as Supplier<String>) == "something"
    ns1Child.getOrDefault("key", String, { shouldNotBeCalled() } as Supplier<String>) == "something"
  }

  def "create object via getOrComputeIfAbsent(Class)"() {
    expect:
    ns1Root.get(ArrayList) == null

    when:
    def list = ns1Root.getOrComputeIfAbsent(ArrayList)
    list.add("something")

    then:
    ns1Root.get(ArrayList) == ["something"]
    ns1Child.get(ArrayList) == ["something"]

    and:
    ns1Root.getOrComputeIfAbsent(ArrayList) == ["something"]
  }

  def "create object via getOrComputeIfAbsent(key, creator)"() {
    expect:
    ns1Root.get("key") == null

    when:
    def list = ns1Root.getOrComputeIfAbsent("key", { [] }) as List
    list.add("something")

    then:
    ns1Root.get("key") == ["something"]
    ns1Child.get("key") == ["something"]

    and:
    ns1Root.getOrComputeIfAbsent("key", { shouldNotBeCalled() }) == ["something"]
  }

  def "create object via getOrComputeIfAbsent(key, creator, type)"() {
    expect:
    ns1Root.get("key") == null

    when:
    def list = ns1Root.getOrComputeIfAbsent("key", { [] }, List)
    list.add("something")

    then:
    ns1Root.get("key") == ["something"]
    ns1Child.get("key") == ["something"]

    and: "create is not invoked if already present"
    ns1Root.getOrComputeIfAbsent("key", { shouldNotBeCalled() }, List) == ["something"]
  }

  def "AutoCloseable items are closed when the store is closed, but not when they have been removed or replaced prior"() {
    given:
    AutoCloseable item = Mock()
    AutoCloseable other = Mock()

    ns1Child.put("item", item)
    ns1Child.put("other", other)
    ns1Child.put("yetAnother", other)

    when:
    ns1Child.remove("other")
    ns1Child.put("yetAnother", "something else")

    and:
    childStore.close()

    then:
    1 * item.close()
    0 * other.close()
  }

  def "removing items returns the stored value"() {
    given:
    ns1Root.put("key", "something")

    expect:
    ns1Root.get("key") == "something"

    when:
    def value = ns1Root.remove("key")

    then:
    ns1Root.get("key") == null
    value == "something"

    when:
    def second = ns1Root.remove("key")

    then:
    ns1Root.get("key") == null
    second == null
  }

  def "removing items returns the stored value with required type"() {
    given:
    ns1Root.put("key", "something")

    expect:
    ns1Root.get("key", String) == "something"

    when:
    def value = ns1Root.remove("key")

    then:
    ns1Root.get("key") == null
    value == "something"

    when:
    def second = ns1Root.remove("key", String)

    then:
    ns1Root.get("key") == null
    second == null
  }

  def "retrieving a stored value with the wrong required type throws a StoreException"() {
    when:
    ns1Root.get("nonexistent", String)

    then:
    noExceptionThrown()

    when:
    ns1Root.put("key", [])
    ns1Root.get("key", String)

    then:
    thrown(IStore.StoreException)
  }

  def "removing a stored value with the wrong required type throws a StoreException"() {
    when:
    ns1Root.remove("nonexistent", String)

    then:
    noExceptionThrown()

    when:
    ns1Root.put("key", [])
    ns1Root.remove("key", String)

    then:
    thrown(IStore.StoreException)
  }

  def "using getOrDefault on a stored value with the wrong required type throws a StoreException"() {
    when:
    ns1Root.put("key", [])
    ns1Root.getOrDefault("key", String, "")

    then:
    thrown(IStore.StoreException)
  }

  def "using getOrComputeIfAbsent on a stored value with the wrong required type throws a StoreException"() {
    when:
    ns1Root.put("key", [])
    ns1Root.getOrComputeIfAbsent("key", { shouldNotBeCalled() }, String)

    then:
    thrown(IStore.StoreException)
  }

  def "returning null default values is valid"() {
    expect:
    ns1Root.getOrDefault("key", String, null as String) == null
    ns1Root.getOrDefault("key", String, { null } as Supplier<String>) == null
    ns1Root.getOrComputeIfAbsent("a", { null }) == null
    ns1Root.getOrComputeIfAbsent("b", { null }, String) == null
  }

  def "null values still count as present"() {
    given:
    ns1Root.put("key", null)

    expect:
    ns1Root.getOrComputeIfAbsent("a", { null }) == null
    ns1Root.getOrComputeIfAbsent("a", { shouldNotBeCalled() }) == null
    ns1Root.getOrComputeIfAbsent("key", { shouldNotBeCalled() }) == null
  }

  def <V> V shouldNotBeCalled() {
    throw new RuntimeException()
  }
}
