package org.spockframework.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class DataVariableMap extends AbstractMap<String, Object> {
  private final List<String> dataVariableNames;
  private final Object[] dataValues;
  private Set<Entry<String, Object>> entrySet;

  public DataVariableMap(List<String> dataVariableNames, Object[] dataValues) {
    this.dataVariableNames = requireNonNull(dataVariableNames);
    this.dataValues = requireNonNull(dataValues);
    if (dataVariableNames.size() != dataValues.length) {
      throw new IllegalArgumentException("the parameters must have the same length");
    }
  }

  @Override
  public int size() {
    return dataValues.length;
  }

  @Override
  public boolean isEmpty() {
    return dataValues.length == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return dataVariableNames.contains(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return Arrays.stream(dataValues).anyMatch(value::equals);
  }

  @Override
  public Object get(Object key) {
    int entryIndex = dataVariableNames.indexOf(key);
    return (entryIndex == -1) ? null : dataValues[entryIndex];
  }

  @Nullable
  @Override
  public Object put(String key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(@NotNull Map<? extends String, ?> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Set<Entry<String, Object>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  private class EntrySet implements Set<Entry<String, Object>> {
    @Override
    public int size() {
      return dataValues.length;
    }

    @Override
    public boolean isEmpty() {
      return dataValues.length == 0;
    }

    @Override
    public boolean contains(Object obj) {
      if (!(obj instanceof Entry)) {
        return false;
      }
      Entry<?, ?> entry = (Entry<?, ?>) obj;
      int entryIndex = dataVariableNames.indexOf(entry.getKey());
      return (entryIndex != -1) && Objects.equals(dataValues[entryIndex], entry.getValue());
    }

    @NotNull
    @Override
    public Iterator<Entry<String, Object>> iterator() {
      return new Iterator<Entry<String, Object>>() {
        private int i = 0;

        @Override
        public boolean hasNext() {
          return i < dataValues.length;
        }

        @Override
        public Entry<String, Object> next() {
          return new SimpleImmutableEntry<>(dataVariableNames.get(i), dataValues[i++]);
        }
      };
    }

    @NotNull
    @Override
    public Object[] toArray() {
      return toArray(new Object[0]);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
      T[] result = (a.length >= dataValues.length)
        ? a
        : (T[]) Array.newInstance(a.getClass().getComponentType(), dataValues.length);
      Iterator<Entry<String, Object>> it = iterator();
      for (int i = 0; i < result.length; i++) {
        if (!it.hasNext()) {
          result[i] = null;
          break;
        }
        result[i] = (T) it.next();
      }
      return result;
    }

    @Override
    public boolean add(Entry<String, Object> stringObjectEntry) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
      return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Entry<String, Object>> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }
  }
}
