/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.condition;

import java.util.regex.Pattern;
import java.util.*;

/**
 * Direct port of the equally named class in the Specs project (http://code.google.com/p/specs/).
 * The original code is here:
 * http://code.google.com/p/specs/source/browse/trunk/src/main/scala/org/specs/util/EditDistance.scala?spec=svn1159&r=1116
 *
 * @author Peter Niederwieser
 */
public class DiffShortener {
  private final String firstSep;
  private final String secondSep;
  private final int size;

  private final String firstSepQuoted;
  private final String secondSepQuoted;

  public DiffShortener(String firstSep, String secondSep, int size) {
    this.firstSep = firstSep;
    this.secondSep = secondSep;
    this.size = size;

    firstSepQuoted = Pattern.quote(firstSep);
    secondSepQuoted = Pattern.quote(secondSep);
  }

  public String shorten(String s) {
    List<String> list = sepList(s);
    StringBuilder res = new StringBuilder();

    for (int i = 0; i < list.size(); i++) {
      String cur = list.get(i);

      if (cur.startsWith(firstSep) && cur.endsWith(secondSep))
        res.append(cur);
      else if (i == 0)
        res.append(shortenLeft(cur));
      else if (i == list.size() - 1)
        res.append(shortenRight(cur));
      else
        res.append(shortenCenter(cur));
    }
    
    return res.toString();
  }

  private String shortenLeft(String s) {
    if (s.length() <= size) return s;
    return "..." + s.substring(s.length() - size);
  }

  private String shortenRight(String s) {
    if (s.length() <= size) return s;
    return s.substring(0, size) + "...";
  }

  private String shortenCenter(String s) {
    if (s.length() <= size) return s;
    return s.substring(0, size / 2) + "..." + s.substring(s.length() - size / 2);
  }

  private List<String> sepList(String s) {
    String[] splitted = s.split(firstSepQuoted);
    if (splitted.length == 1)
      return Collections.singletonList(splitted[0]);

    List<String> res = new ArrayList<String>();
    for (String cur : splitted) {
      if (!cur.contains(secondSep)) {
        res.add(cur);
      } else {
        String[] curSplitted = cur.split(secondSepQuoted);
        if (curSplitted.length == 0)
          res.add(firstSep + secondSep);
        else {
          res.add(firstSep + curSplitted[0] + secondSep);
          if (curSplitted.length > 1) res.add(curSplitted[1]);
        }
      }
    }

    return res;
  }
}
