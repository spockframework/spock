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

  protected ByteArrayOutputStream swappedInOutByteStream
  protected ByteArrayOutputStream swappedInErrByteStream
  
  protected boolean swapped = false
  
  List<ByteArrayOutputStream> swapIn() {
    if (swapped) throw new IllegalStateException("swapIn() called during a swap")
    
    swappedOutOut = System.out
    swappedOutErr = System.err

    swappedInOutByteStream = new ByteArrayOutputStream()
    swappedInErrByteStream = new ByteArrayOutputStream()
    
    swappedInOut = new PrintStream(swappedInOutByteStream)
    swappedInErr = new PrintStream(swappedInErrByteStream)
    
    System.out = swappedInOut
    System.err = swappedInErr
    
    swapped = true

    [swappedInOutByteStream, swappedInErrByteStream]
  }
  
  List<ByteArrayOutputStream> swapOut() {
    if (!swapped) throw new IllegalStateException("swapOut() called while not during a swap")
    
    System.out = swappedOutOut
    System.err = swappedOutErr

    swappedOutOut = null
    swappedOutErr = null

    swappedInOut = null
    swappedInErr = null

    def byteStreams = [swappedInOutByteStream, swappedInErrByteStream]
    swappedInOutByteStream = null
    swappedInErrByteStream = null
    
    swapped = false
    
    byteStreams
  }
  
}