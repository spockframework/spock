package org.spockframework.smoke.mock;

/**
 * @author Alexey Elin
 * @version 1.0 19.01.2018
 */
public interface IFactory {

  static IFactory instance() {
    return new IFactory() {
      @Override
      public Object make() {
        return new Object();
      }
    };
  }

  Object make();
}
