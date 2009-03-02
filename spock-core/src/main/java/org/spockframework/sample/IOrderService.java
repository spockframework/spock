package org.spockframework.sample;

import java.util.List;

/**
 *
 * @author Peter Niederwieser
 */
public interface IOrderService {
  boolean isOnline();
  void dispatch(List<String> items);
  int getDispatchedItemCount();
}
