/*
 *  Copyright 2026 the original author or authors.
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

package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Generates the {@code llms.txt} documentation index from a published {@code docs/} directory.
 *
 * <p>{@code docsDir} is {@link Internal} because the output {@code llms.txt} lives inside it and
 * the task runs against a freshly checked-out worktree; callers wire it with
 * {@code outputs.upToDateWhen { false }}.
 */
@CompileStatic
abstract class GenerateLlmsTxt extends DefaultTask {
  @Internal
  abstract DirectoryProperty getDocsDir()

  @Input
  abstract Property<String> getBaseUrl()

  @OutputFile
  abstract RegularFileProperty getOutputFile()

  @TaskAction
  void generate() {
    def docs = docsDir.get().asFile.toPath()
    outputFile.get().asFile.toPath().text = LlmsTxtRenderer.render(docs, baseUrl.get())
  }
}
