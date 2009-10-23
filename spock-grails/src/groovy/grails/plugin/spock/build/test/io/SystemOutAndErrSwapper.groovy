/*
 * Copyright 2009 the original author or authors.
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

package grails.plugin.spock.build.test.io

// TODO this class needs a better name and method name
class SystemOutAndErrSwapper {
  protected swappedOutOut
  protected swappedOutErr

  protected swappedInOut
  protected swappedInErr

  protected swapped = false
  
  List<OutputStream> swapIn() {
    if (swapped) throw new IllegalStateException("swapIn() called during a swap")
    
    swappedOutOut = System.out
    swappedOutErr = System.err

    swappedInOut = new ByteArrayOutputStream()
    swappedInErr = new ByteArrayOutputStream()

    System.setOut(new PrintStream(swappedInOut))
    System.setErr(new PrintStream(swappedInErr))
    
    swapped = true

    [this.swappedInOut, this.swappedInErr]
  }
  
  List<OutputStream> swapOut() {
    if (!swapped) throw new IllegalStateException("swapOut() called while not during a swap")
    
    System.out = this.swappedOutOut
    System.err = this.swappedOutErr

    swappedOutOut = null
    swappedOutErr = null

    def streams = [this.swappedInOut, this.swappedInErr]
    swappedInOut = null
    swappedInErr = null
    
    swapped = false
    
    streams
  }
  
  def swap(Closure swappedFor) {
    def streams = swapIn()
    
    try {
      switch (swappedFor.maximumNumberOfParameters) {
        case 0:
          swappedFor()
          break
        case 1:
          swappedFor(streams)
          break
        default:
          swappedFor(*streams)
          break
      }
    } finally {
      swapOut()
    }

    streams
  }
}