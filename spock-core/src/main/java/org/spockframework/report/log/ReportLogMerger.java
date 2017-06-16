/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.report.log;

import org.spockframework.util.*;

import java.util.*;

class ReportLogMerger {
  @SuppressWarnings("unchecked")
  public <T> T merge(T obj, T update) {
    if (obj instanceof Map) {
      return (T) mergeMap((Map) obj, (Map) update);
    }
    if (obj instanceof List) {
      return (T) mergeList((List) obj, (List) update);
    }
    return update;
  }

  public Map<String, Object> mergeMap(Map<String, Object> map, Map<String, Object> update) {
    if (map == null) return update;

    for (Map.Entry<String, Object> entry : update.entrySet()) {
      map.put(entry.getKey(), merge(map.get(entry.getKey()), entry.getValue()));
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  public List<Object> mergeList(List<Object> list, List<Object> update) {
    if (list == null) return update;

    if (isNameIndexed(list)) {
      return mergeNameIndexed((List) list, (List) update);
    }

    list.addAll(update);
    return list;
  }

  private boolean isNameIndexed(List<Object> list) {
    return !list.isEmpty() && list.get(0) instanceof Map && ((Map) list.get(0)).containsKey("name");
  }

  private List mergeNameIndexed(List<Map<String, Object>> list, List<Map<String, Object>> update) {
    for (final Map<String, Object> fromElem : update) {
      int index = CollectionUtil.findIndexOf(list, new IFunction<Map<String, Object>, Boolean>() {
        @Override
        public Boolean apply(Map<String, Object> map) {
          return map.get("name").equals(fromElem.get("name"));
        }
      });

      if (index != -1) {
        list.set(index, mergeMap(list.get(index), fromElem));
      } else {
        list.add(fromElem);
      }
    }

    return list;
  }
}
