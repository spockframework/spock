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

import java.util.List;

import org.spockframework.util.TextUtil;

import static org.spockframework.runtime.condition.EditOperation.Kind.*;

public class StringDifferenceRenderer {
  public String render(String str1, String str2, List<EditOperation> ops) {
    int index1 = 0;
    int index2 = 0;

    StringBuilder line1 = new StringBuilder();
    StringBuilder line2 = new StringBuilder();

    EditOperation.Kind prevKind = SKIP;

    for (EditOperation op : ops) {
      if (prevKind == SKIP ^ op.getKind() == SKIP) {
        String separator = prevKind == SKIP ? "(" : ")";
        line1.append(separator);
        line2.append(separator);
      }

      switch (op.getKind()) {
        case DELETE:
          line1.append(str1.substring(index1, index1 + op.getLength()));
          line2.append(TextUtil.repeatChar('-', op.getLength()));
          index1 += op.getLength();
          break;
        case INSERT:
          line1.append(TextUtil.repeatChar('-', op.getLength()));
          line2.append(str2.substring(index2, index2 + op.getLength()));
          index2 += op.getLength();
          break;
        case SKIP:
        case SUBSTITUTE:
          line1.append(str1.substring(index1, index1 + op.getLength()));
          line2.append(str2.substring(index2, index2 + op.getLength()));
          index1 += op.getLength();
          index2 += op.getLength();
          break;
      }
      prevKind = op.getKind();
    }

    if (prevKind != SKIP) {
      line1.append(")");
      line2.append(")");
    }

    return line1.toString() + "\n" + line2.toString();
  }
}
