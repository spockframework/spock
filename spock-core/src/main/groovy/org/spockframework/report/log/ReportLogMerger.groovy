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

package org.spockframework.report.log

class ReportLogMerger {
  Map merge(Map map, Map update) {
    if (!map) return update

    update.each { key, value ->
      map[key] = merge(map[key], value)
    }
    return map
  }

  List merge(List list, List update) {
    if (!list) return update

    if (isNameIndexed(list)) {
      return mergeNameIndexed(list, update)
    }

    list.addAll(update)
    return list
  }

  Object merge(Object object, Object update) {
    update
  }

  private boolean isNameIndexed(List list) {
    !list.isEmpty() && list[0] instanceof Map && list[0].containsKey("name")
  }

  private List mergeNameIndexed(List list, List update) {
    for (fromElem in update) {
      def index = list.findIndexOf { it.name == fromElem.name }
      if (index != -1) {
        list[index] = merge(list[index], fromElem)
      } else {
        list << fromElem
      }
    }
    return list
  }
}
