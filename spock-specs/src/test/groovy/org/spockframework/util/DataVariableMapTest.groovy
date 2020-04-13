package org.spockframework.util

import spock.lang.Specification

import java.util.AbstractMap.SimpleEntry

class DataVariableMapTest extends Specification {
  def testee = new DataVariableMap(
    ['a', 'b'],
    1, 2
  )

  def 'modifying methods throw exception'() {
    when:
    method(testee)

    then:
    thrown(UnsupportedOperationException)

    where:
    _ | method
    _ | { it.put('foo', _) }
    _ | { it.remove(_) }
    _ | { it.putAll(foo: _) }
    _ | { it.clear() }
    _ | { it.entrySet().add(new SimpleEntry<>('foo', _)) }
    _ | { it.entrySet().remove(_) }
    _ | { it.entrySet().addAll([new SimpleEntry<>('foo', _)]) }
    _ | { it.entrySet().retainAll([]) }
    _ | { it.entrySet().removeAll([]) }
    _ | { it.entrySet().clear() }
  }

  def 'size returns proper result'() {
    expect:
    testee.size() == 2
  }

  def 'isEmpty returns proper result'() {
    expect:
    testee.isEmpty() == result

    where:
    testee                        || result
    new DataVariableMap([])       || true
    new DataVariableMap(['a'], 1) || false
  }

  def 'containsKey returns proper result'() {
    expect:
    testee.containsKey(key) == result

    where:
    key   || result
    'a'   || true
    'b'   || true
    'foo' || false
    null  || false
  }

  def 'containsValue returns proper result'() {
    expect:
    testee.containsValue(value) == result

    where:
    value || result
    1     || true
    2     || true
    0     || false
  }

  def 'get returns proper result'() {
    expect:
    testee.get(key) == result

    where:
    key   || result
    'a'   || 1
    'b'   || 2
    'foo' || null
    null  || null
  }

  def 'entrySet size returns proper result'() {
    expect:
    testee.entrySet().size() == 2
  }

  def 'entrySet isEmpty returns proper result'() {
    expect:
    testee.entrySet().empty == result

    where:
    testee                        || result
    new DataVariableMap([])       || true
    new DataVariableMap(['a'], 1) || false
  }

  def 'entrySet contains returns proper result'() {
    expect:
    testee.entrySet().contains(new SimpleEntry(key, value)) == result

    where:
    key   | value || result
    'a'   | 1     || true
    'a'   | 2     || false
    'a'   | null  || false
    'b'   | 2     || true
    'foo' | 1     || false
    null  | 1     || false
  }

  def 'entrySet iterator returns proper result'() {
    expect:
    testee.entrySet().iterator().collect() == [
      new SimpleEntry<>('a', 1),
      new SimpleEntry<>('b', 2)
    ]
  }

  def 'entrySet toArray returns proper result'() {
    expect:
    testee.entrySet().toArray() == [
      new SimpleEntry<>('a', 1),
      new SimpleEntry<>('b', 2)
    ] as Object[]
  }

  def 'entrySet typed toArray returns proper result'() {
    expect:
    testee.entrySet().toArray([] as Object[]) == [
      new SimpleEntry<>('a', 1),
      new SimpleEntry<>('b', 2)
    ] as Object[]
  }

  def 'entrySet containsAll returns proper result'() {
    expect:
    testee.entrySet().containsAll([
      new SimpleEntry<>('a', 1),
      new SimpleEntry<>('b', 2)
    ])
  }
}
