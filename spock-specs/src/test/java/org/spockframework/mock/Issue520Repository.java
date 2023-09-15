package org.spockframework.mock;

import java.io.Serializable;

public interface Issue520Repository {
  //Note: we need to compile this with Java, because Groovy 2.5 can't compile this:
  //unexpected token: <E extends Object, ID extends Serializable> ID persist(E e);
  <E extends Object, ID extends Serializable> ID persist(E e);
}
