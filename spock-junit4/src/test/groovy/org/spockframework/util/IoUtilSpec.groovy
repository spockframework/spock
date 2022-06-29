/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class IoUtilSpec extends Specification {
  @Rule TemporaryFolder tempDir

  def "get file extension"() {
    expect:
    IoUtil.getFileExtension("foo/bar/my.file.ext") == "ext"
    IoUtil.getFileExtension("foo/bar/myfile") == null
  }

  def "list files recursively"() {
    def file1 = tempDir.newFile("file1")
    def file2 = tempDir.newFile("file2")
    def file3 = tempDir.newFile("file3")
    def dir = tempDir.newFolder("dir")
    def dir_file1 = new File(dir, "file1")
    def dir_file2 = new File(dir, "file2")
    def dir_file3 = new File(dir, "file3")
    [dir_file1, dir_file2, dir_file3]*.createNewFile()

    expect:
    IoUtil.listFilesRecursively(tempDir.root) as Set == [file1, file2, file3, dir_file1, dir_file2, dir_file3] as Set
  }
}
