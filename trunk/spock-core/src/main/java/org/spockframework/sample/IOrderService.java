package org.spockframework.sample;

import java.util.List;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
public interface IOrderService {
  boolean isOnline();
  void dispatch(List<String> items);
  int getDispatchedItemCount();
}
