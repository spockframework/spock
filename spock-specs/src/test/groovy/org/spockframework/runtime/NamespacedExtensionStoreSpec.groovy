/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime

import org.spockframework.runtime.extension.IStore
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.util.function.Supplier

class NamespacedExtensionStoreSpec extends Specification {
  static final NS1 = IStore.Namespace.create("NS1")
  static final NS2 = IStore.Namespace.create("NS2")

  @AutoCleanup
  def rootStoreProvider = StoreProvider.createRootStoreProvider()
  @AutoCleanup
  def childStoreProvider = rootStoreProvider.createChildStoreProvider()

  def ns1Root = rootStoreProvider.getStore(NS1)
  def ns1Child = childStoreProvider.getStore(NS1)

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
    ns1Child.put("key", "other thing")

    then:
    ns1Root.get("key") == "something"
    ns1Child.get("key", String) == "other thing"

    when:
    ns1Child.remove("key")

    then:
    ns1Child.get("key") == "something"
  }

  def "stores with different namespaces don't leak into each other"() {
    given:
    ns1Root.put("key", "something")

    and: "another store"
    def ns2Store = rootStoreProvider.getStore(NS2)

    and: "another store with an appended namespace"
    def ns1Sub = rootStoreProvider.getStore(NS1.append("sub"))

    expect:
    ns2Store.get("key") == null
    ns1Sub.get("key") == null
  }


  def "returns default value if absent"() {
    expect:
    ns1Root.getOrDefault("key", String, "default") == "default"

    and: "value is still missing"
    ns1Root.get("key") == null
  }

  def "returns default value from supplier if absent"() {
    expect:
    // explicit cast is necessary as groovy dynamic dispatch would otherwise treat the closure as default value 🤦
    ns1Root.getOrDefault("key", String, { "default" } as Supplier<String>) == "default"

    and: "value is still missing"
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

  def "AutoCloseable items are closed in reverse insertion order when the store is closed, but not when they have been removed or replaced prior"() {
    given:
    AutoCloseable item = Mock()
    AutoCloseable item2 = Mock()
    AutoCloseable item3 = Mock()
    AutoCloseable other = Mock()

    ns1Child.put("item", item)
    ns1Child.put("item2", item2)
    ns1Child.put("item3", item3)
    ns1Child.put("other", other)
    ns1Child.put("yetAnother", other)

    when:
    ns1Child.remove("other")
    ns1Child.put("yetAnother", "something else")

    and:
    childStoreProvider.close()

    then:
    1 * item3.close()

    then:
    1 * item2.close()

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

  def "root stores have no parent and return themselves as root"() {
    expect:
    ns1Root.parentStore == null
    ns1Root.rootStore == ns1Root
  }

  def "child stores can navigate to the parent and root store"() {
    given:
    def grandChildStoreProvider = childStoreProvider.createChildStoreProvider()
    def ns1GrandChild = grandChildStoreProvider.getStore(NS1)
    ns1Root.put("key", "something")
    ns1Child.put("key", "other thing")
    ns1GrandChild.put("key", "grand thing")

    expect:
    ns1Root.get("key") == "something"

    and:
    ns1Child.get("key") == "other thing"
    ns1Child.parentStore.get("key") == "something"
    ns1Child.rootStore.get("key") == "something"

    and:
    ns1GrandChild.get("key") == "grand thing"
    ns1GrandChild.parentStore.get("key") == "other thing"
    ns1GrandChild.rootStore.get("key") == "something"
  }

  def "namespaces have readable toString"() {
    expect:
    NS1.toString() == "Namespace{parts=[NS1]}"
  }

  def <V> V shouldNotBeCalled() {
    throw new RuntimeException()
  }
}
