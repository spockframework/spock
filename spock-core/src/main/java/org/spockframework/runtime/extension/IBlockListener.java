package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.BlockInfo;
import org.spockframework.runtime.model.IterationInfo;

public interface IBlockListener {
  default void blockEntered(IterationInfo iterationInfo, BlockInfo blockInfo) {}
  default void blockExited(IterationInfo iterationInfo, BlockInfo blockInfo) {}
}
