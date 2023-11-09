package org.spockframework.runtime.extension;

import org.spockframework.util.Beta;
import org.spockframework.util.Checks;
import org.spockframework.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A hierarchical store of key-value pairs.
 * @since 2.4
 */
@Beta
public interface IStore {
  /**
   * Get the value that is stored under the supplied {@code key}.
   *
   * <p>If no value is stored in the current {@link IStore}
   * for the supplied {@code key}, ancestors of the context will be queried
   * for a value with the same {@code key} in the {@code Namespace} used
   * to create this store.
   *
   * <p>For greater type safety, consider using {@link #get(Object, Class)}
   * instead.
   *
   * @param key the key; never {@code null}
   * @return the value; potentially {@code null}
   * @see #get(Object, Class)
   * @see #getOrDefault(Object, Class, Object)
   */
  Object get(Object key);

  /**
   * Get the value of the specified required type that is stored under
   * the supplied {@code key}.
   *
   * <p>If no value is stored in the current {@link IStore}
   * for the supplied {@code key}, ancestors of the context will be queried
   * for a value with the same {@code key} in the {@code Namespace} used
   * to create this store.
   *
   * @param key          the key; never {@code null}
   * @param requiredType the required type of the value; never {@code null}
   * @param <V>          the value type
   * @return the value; potentially {@code null}
   * @see #get(Object)
   * @see #getOrDefault(Object, Class, Object)
   */
  <V> V get(Object key, Class<V> requiredType);

  /**
   * Get the value of the specified required type that is stored under
   * the supplied {@code key}, or the supplied {@code defaultValue} if no
   * value is found for the supplied {@code key} in this store or in an
   * ancestor.
   *
   * <p>If no value is stored in the current {@link IStore}
   * for the supplied {@code key}, ancestors of the context will be queried
   * for a value with the same {@code key} in the {@code Namespace} used
   * to create this store.
   *
   * @param key          the key; never {@code null}
   * @param requiredType the required type of the value; never {@code null}
   * @param defaultValue the default value
   * @param <V>          the value type
   * @return the value; potentially {@code null}
   * @see #get(Object, Class)
   */
  default <V> V getOrDefault(Object key, Class<V> requiredType, V defaultValue) {
    V value = get(key, requiredType);
    return (value != null ? value : defaultValue);
  }

  /**
   * Get the object of type {@code type} that is present in this
   * {@code Store} (<em>keyed</em> by {@code type}); and otherwise invoke
   * the default constructor for {@code type} to generate the object,
   * store it, and return it.
   *
   * <p>This method is a shortcut for the following, where {@code X} is
   * the type of object we wish to retrieve from the store.
   *
   * <pre style="code">
   * X x = store.getOrComputeIfAbsent(X.class, key -&gt; new X(), X.class);
   * // Equivalent to:
   * // X x = store.getOrComputeIfAbsent(X.class);
   * </pre>
   *
   * <p>See {@link #getOrComputeIfAbsent(Object, Function, Class)} for
   * further details.
   *
   * <p>If {@code type} implements {@link AutoCloseable}
   * the {@code close()} method will be invoked on the stored object when
   * the store is closed.
   *
   * @param type the type of object to retrieve; never {@code null}
   * @param <V>  the key and value type
   * @return the object; never {@code null}
   * @see #getOrComputeIfAbsent(Object, Function)
   * @see #getOrComputeIfAbsent(Object, Function, Class)
   * @see AutoCloseable
   */
  default <V> V getOrComputeIfAbsent(Class<V> type) {
    return getOrComputeIfAbsent(type, ReflectionUtil::newInstance, type);
  }

  /**
   * Get the value that is stored under the supplied {@code key}.
   *
   * <p>If no value is stored in the current {@link IStore}
   * for the supplied {@code key}, ancestors of the context will be queried
   * for a value with the same {@code key} in the {@code Namespace} used
   * to create this store. If no value is found for the supplied {@code key},
   * a new value will be computed by the {@code defaultCreator} (given
   * the {@code key} as input), stored, and returned.
   *
   * <p>For greater type safety, consider using
   * {@link #getOrComputeIfAbsent(Object, Function, Class)} instead.
   *
   * <p>If the created value is an instance of {@link AutoCloseable}
   * the {@code close()} method will be invoked on the stored object when
   * the store is closed.
   *
   * @param key            the key; never {@code null}
   * @param defaultCreator the function called with the supplied {@code key}
   *                       to create a new value; never {@code null}
   * @param <K>            the key type
   * @param <V>            the value type
   * @return the value; potentially {@code null}
   * @see #getOrComputeIfAbsent(Class)
   * @see #getOrComputeIfAbsent(Object, Function, Class)
   * @see AutoCloseable
   */
  <K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator);

  /**
   * Get the value of the specified required type that is stored under the
   * supplied {@code key}.
   *
   * <p>If no value is stored in the current {@link IStore}
   * for the supplied {@code key}, ancestors of the context will be queried
   * for a value with the same {@code key} in the {@code Namespace} used
   * to create this store. If no value is found for the supplied {@code key},
   * a new value will be computed by the {@code defaultCreator} (given
   * the {@code key} as input), stored, and returned.
   *
   * <p>If {@code requiredType} implements {@link AutoCloseable}
   * the {@code close()} method will be invoked on the stored object when
   * the store is closed.
   *
   * @param key            the key; never {@code null}
   * @param defaultCreator the function called with the supplied {@code key}
   *                       to create a new value; never {@code null}
   * @param requiredType   the required type of the value; never {@code null}
   * @param <K>            the key type
   * @param <V>            the value type
   * @return the value; potentially {@code null}
   * @see #getOrComputeIfAbsent(Class)
   * @see #getOrComputeIfAbsent(Object, Function)
   * @see AutoCloseable
   */
  <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType);

  /**
   * Store a {@code value} for later retrieval under the supplied {@code key}.
   *
   * <p>A stored {@code value} is visible in child {@link IStore
   * ExtensionContexts} for the store's {@code Namespace} unless they
   * overwrite it.
   *
   * <p>If the {@code value} is an instance of {@link AutoCloseable}
   * the {@code close()} method will be invoked on the stored object when
   * the store is closed.
   *
   * @param key   the key under which the value should be stored; never
   *              {@code null}
   * @param value the value to store; may be {@code null}
   * @return the previous value; may be {@code null}
   * @see AutoCloseable
   */
  Object put(Object key, Object value);

  /**
   * Remove the value that was previously stored under the supplied {@code key}.
   *
   * <p>The value will only be removed in the current {@link IStore},
   * not in ancestors. In addition, the {@link AutoCloseable} API will not
   * be honored for values that are manually removed via this method.
   *
   * <p>For greater type safety, consider using {@link #remove(Object, Class)}
   * instead.
   *
   * @param key the key; never {@code null}
   * @return the previous value or {@code null} if no value was present
   * for the specified key
   * @see #remove(Object, Class)
   */
  Object remove(Object key);

  /**
   * Remove the value of the specified required type that was previously stored
   * under the supplied {@code key}.
   *
   * <p>The value will only be removed in the current {@link IStore},
   * not in ancestors. In addition, the {@link AutoCloseable} API will not
   * be honored for values that are manually removed via this method.
   *
   * @param key          the key; never {@code null}
   * @param requiredType the required type of the value; never {@code null}
   * @param <V>          the value type
   * @return the previous value or {@code null} if no value was present
   * for the specified key
   * @see #remove(Object)
   */
  <V> V remove(Object key, Class<V> requiredType);

  /**
   * A {@code Namespace} is used to provide a <em>scope</em> for data saved by
   * extensions within an {@link IStore}.
   *
   * <p>Storing data in custom namespaces allows extensions to avoid accidentally
   * mixing data between extensions or across different invocations within the
   * lifecycle of a single extension.
   */
  class Namespace {

    /**
     * The default, global namespace which allows access to stored data from
     * all extensions.
     */
    public static final Namespace GLOBAL = Namespace.create(new Object());

    /**
     * Create a namespace which restricts access to data to all extensions
     * which use the same sequence of {@code parts} for creating a namespace.
     *
     * <p>The order of the {@code parts} is significant.
     *
     * <p>Internally the {@code parts} are compared using {@link Object#equals(Object)}.
     */
    public static Namespace create(Object... parts) {
      Checks.notEmpty(parts, () -> "parts array must not be null or empty");
      Checks.containsNoNullElements(parts, () -> "individual parts must not be null: " + Arrays.toString(parts));
      return new Namespace(new ArrayList<>(Arrays.asList(parts)));
    }

    private final List<Object> parts;

    private Namespace(List<Object> parts) {
      this.parts = parts;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Namespace that = (Namespace) o;
      return this.parts.equals(that.parts);
    }

    @Override
    public int hashCode() {
      return this.parts.hashCode();
    }

    /**
     * Create a new namespace by appending the supplied {@code parts} to the
     * existing sequence of parts in this namespace.
     *
     * @return new namespace; never {@code null}
     */
    public Namespace append(Object... parts) {
      Checks.notEmpty(parts, () -> "parts array must not be null or empty");
      Checks.containsNoNullElements(parts, () -> "individual parts must not be null: " + Arrays.toString(parts));
      ArrayList<Object> newParts = new ArrayList<>(this.parts.size() + parts.length);
      newParts.addAll(this.parts);
      Collections.addAll(newParts, parts);
      return new Namespace(newParts);
    }
  }
}
