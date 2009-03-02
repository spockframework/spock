package org.spockframework.sample;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Peter Niederwieser
 */
public class ShoppingCart {
  private IOrderService service;
  private final List<String> items = new ArrayList<String>();

  public void setOrderService(IOrderService service) {
    this.service = service;
  }

  public void addItem(String item) {
    items.add(item);
  }

  public void checkOut(int times) {
    if (!service.isOnline())
      throw new RuntimeException("service not available");

    for (int i = 0; i < times; i++)
      service.dispatch(items);
  }
}
