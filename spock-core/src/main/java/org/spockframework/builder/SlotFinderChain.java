package org.spockframework.builder;

import java.lang.reflect.Type;
import java.util.List;

public class SlotFinderChain implements ISlotFinder {
  private final List<? extends ISlotFinder> slotFinders;

  public SlotFinderChain(List<? extends ISlotFinder> slotFinders) {
    this.slotFinders = slotFinders;
  }

  public ISlot find(String name, Object owner, Type ownerType) {
    for (ISlotFinder finder : slotFinders) {
      ISlot result = finder.find(name, owner, ownerType);
      if (result != null) return result;
    }
    return null;
  }
}
