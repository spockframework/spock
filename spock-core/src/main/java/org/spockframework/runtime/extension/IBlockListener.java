package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.BlockInfo;
import org.spockframework.runtime.model.IterationInfo;

public interface IBlockListener {
  void blockEntered(IterationInfo iterationInfo, BlockInfo blockInfo);
}
