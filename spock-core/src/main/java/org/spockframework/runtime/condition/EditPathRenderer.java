/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.condition;

import java.util.List;

import org.spockframework.util.TextUtil;

import static org.spockframework.runtime.condition.EditOperation.Kind.SKIP;

public class EditPathRenderer {
  public String render(CharSequence seq1, CharSequence seq2, List<EditOperation> ops) {
    int index1 = 0;
    int index2 = 0;

    StringBuilder line1 = new StringBuilder();
    StringBuilder line2 = new StringBuilder();

    EditOperation.Kind prevKind = SKIP;

    for (EditOperation op : ops) {
      if (prevKind == SKIP ^ op.getKind() == SKIP) {
        String delimiter = prevKind == SKIP ? "(" : ")";
        line1.append(delimiter);
        line2.append(delimiter);
      }

      switch (op.getKind()) {
        case DELETE:
          for (int i = 0; i < op.getLength(); i++) {
            String part = TextUtil.escape(seq1.charAt(index1++));
            line1.append(part);
            line2.append(part.length() == 1 ? "-" : "-~");
          }
          break;
        case INSERT:
          for (int i = 0; i < op.getLength(); i++) {
            String part = TextUtil.escape(seq2.charAt(index2++));
            line1.append(part.length() == 1 ? "-" : "-~");
            line2.append(part);
          }
          break;
        case SKIP:
        case SUBSTITUTE:
          for (int i = 0; i < op.getLength(); i++) {
            String part1 = TextUtil.escape(seq1.charAt(index1++));
            String part2 = TextUtil.escape(seq2.charAt(index2++));
            line1.append(part1);
            line2.append(part2);
            if (part1.length() < part2.length()) line1.append('~');
            else if (part2.length() < part1.length()) line2.append('~');
          }
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
