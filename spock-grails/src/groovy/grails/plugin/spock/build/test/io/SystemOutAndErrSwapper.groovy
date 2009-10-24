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

class SystemOutAndErrSwapper {
    
  protected PrintStream swappedOutOut
  protected PrintStream swappedOutErr

  protected PrintStream swappedInOut
  protected PrintStream swappedInErr

  protected OutputStream swappedInOutStream
  protected OutputStream swappedInErrStream
  
  protected boolean swapped = false
  
  List<OutputStream> swapIn(outStream = new ByteArrayOutputStream(), errStream = new ByteArrayOutputStream()) {
    if (swapped) throw new IllegalStateException("swapIn() called during a swap")
    
    swappedOutOut = System.out
    swappedOutErr = System.err

    swappedInOutStream = outStream
    swappedInErrStream = errStream
    
    swappedInOut = new PrintStream(swappedInOutStream)
    swappedInErr = new PrintStream(swappedInErrStream)
    
    System.out = swappedInOut
    System.err = swappedInErr
    
    swapped = true

    [swappedInOutStream, swappedInErrStream]
  }
  
  List<OutputStream> swapOut() {
    if (!swapped) throw new IllegalStateException("swapOut() called while not during a swap")
    
    System.out = swappedOutOut
    System.err = swappedOutErr

    swappedOutOut = null
    swappedOutErr = null

    swappedInOut = null
    swappedInErr = null

    def streams = [swappedInOutStream, swappedInErrStream]
    swappedInOutStream = null
    swappedInErrStream = null
    
    swapped = false
    
    streams
  }
  
}