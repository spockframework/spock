package org.spockframework.runtime;

import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

public interface SpecProcessor {
  void process(Collection<SpecInfo> specs);
}
