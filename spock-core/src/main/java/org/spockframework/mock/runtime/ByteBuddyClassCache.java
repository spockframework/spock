package org.spockframework.mock.runtime;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ByteBuddyClassCache extends ReferenceQueue<ClassLoader> {

  private static final ClassLoader MARKER = new URLClassLoader(new URL[0], null); // null cannot be stored in concurrent maps.

  private final boolean weakKey;

  final ConcurrentMap<Key, ConcurrentMap<MockKey, Reference<Class<?>>>> cache = new ConcurrentHashMap<Key, ConcurrentMap<MockKey, Reference<Class<?>>>>();

  public ByteBuddyClassCache(boolean weakKey) {
    this.weakKey = weakKey;
  }

  public Class<?> find(ClassLoader classLoader, Class<?> type, Collection<? extends Class<?>> interfaces) {
    if (classLoader == null) {
      classLoader = MARKER;
    }
    Map<MockKey, Reference<Class<?>>> value = cache.get(new LookupKey(classLoader));
    if (value == null) {
      return null;
    }
    Reference<Class<?>> reference = value.get(MockKey.of(type, interfaces));
    if (reference == null) {
      return null;
    } else {
      return reference.get();
    }
  }

  public void insert(ClassLoader classLoader, Class<?> type, Collection<? extends Class<?>> interfaces, Class<?> enhancedType) {
    if (classLoader == null) {
      classLoader = MARKER;
    }
    ConcurrentMap<MockKey, Reference<Class<?>>> value = cache.get(new LookupKey(classLoader));
    if (value == null) {
      value = new ConcurrentHashMap<MockKey, Reference<Class<?>>>();
      cache.put(new WeakKey(classLoader, this), value);
    }
    value.put(MockKey.of(type, interfaces), weakKey ? new WeakReference<Class<?>>(enhancedType) : new SoftReference<Class<?>>(enhancedType));
  }

  private interface Key {

    ClassLoader get();
  }

  private static class LookupKey implements Key {

    private final ClassLoader value;

    private final int hashCode;

    public LookupKey(ClassLoader value) {
      this.value = value;
      hashCode = System.identityHashCode(value);
    }

    @Override
    public ClassLoader get() {
      return value;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Key)) return false;
      return value == ((Key) object).get();
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }

  private static class WeakKey extends WeakReference<ClassLoader> implements Key {

    private final int hashCode;

    public WeakKey(ClassLoader referent, ReferenceQueue<ClassLoader> q) {
      super(referent, q);
      hashCode = System.identityHashCode(referent);
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Key)) return false;
      return get() == ((Key) object).get();
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }

  // should be stored as a weak or soft reference
  private static class MockKey<T> {
    private final String mockedType;
    private final Set<String> types;

    private MockKey(Class<T> mockedType, Collection<? extends Class<?>> interfaces) {
      this.mockedType = mockedType.getName();
      if (interfaces.isEmpty()) { // Optimize memory footprint for the common case.
        types = Collections.emptySet();
      } else {
        types = new HashSet<String>();
        for (Class<?> anInterface : interfaces) {
          types.add(anInterface.getName());
        }
        types.add(this.mockedType);
      }
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) return true;
      if (other == null || getClass() != other.getClass()) return false;

      MockKey mockKey = (MockKey<?>) other;

      if (!mockedType.equals(mockKey.mockedType)) return false;
      if (!types.equals(mockKey.types)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = mockedType.hashCode();
      result = 31 * result + types.hashCode();
      return result;
    }

    public static <T> MockKey<T> of(Class<T> mockedType, Collection<? extends Class<?>> interfaces) {
      return new MockKey<T>(mockedType, interfaces);
    }
  }
}
