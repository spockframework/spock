/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.spockframework.smoke.mock;

public interface IEither<L, R> {
  class Left<L, R> implements IEither<L, R> {
    private final L value;

    public Left(L value) {
      this.value = value;
    }

    public L value() {
      return value;
    }
  }

  class Right<L, R> implements IEither<L, R> {
    private final R value;

    public Right(R value) {
      this.value = value;
    }

    public R value() {
      return value;
    }
  }

  static <L, R> IEither<L, R> left(L value) {
    return new Left<>(value);
  }

  static <L, R> IEither<L, R> right(R value) {
    return new Right<>(value);
  }
}
