#!/usr/bin/env kotlin

/*
 * Copyright 2023 the original author or authors.
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

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:0.40.1")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.Cron
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.Schedule
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.github
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

workflow(
    name = "Code scanning - action",
    on = listOf(
        Push(
            branches = listOf("!dependabot/**")
        ),
        PullRequest(),
        Schedule(
            listOf(
                Cron(
                    minute = "0",
                    hour = "15",
                    dayWeek = "TUE"
                )
            )
        )
    ),
    sourceFile = __FILE__.toPath(),
    //# https://stackoverflow.com/a/72408109/16358266
    concurrency = Concurrency(
        group = "${expr { github.workflow }}-${expr("${github.eventPullRequest.pull_request.number} || ${github.ref}")}",
        cancelInProgress = true
    )
) {
    job(
        id = "codeql-build",
        name = "CodeQL-Build",
        // CodeQL runs on UbuntuLatest, WindowsLatest, and MacOSLatest
        runsOn = UbuntuLatest,
        _customArguments = mapOf(
            "strategy" to mapOf(
                "fail-fast" to false,
                "matrix" to mapOf(
                    "variant" to listOf("2.5", "3.0", "4.0")
                )
            )
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3()
        )
        // Manually added: Install and setup JDK
        uses(
            name = "Set up JDKs",
            action = CustomAction(
                actionOwner = ".github",
                actionName = "actions/setup-build-env",
                actionVersion = "v0",
                inputs = emptyMap()
            ),
            _customArguments = mapOf(
                "uses" to "./.github/actions/setup-build-env"
            )
        )
        // Initializes the CodeQL tools for scanning
        uses(
            name = "Initialize CodeQL",
            action = CustomAction(
                actionOwner = "github",
                actionName = "codeql-action/init",
                actionVersion = "v3",
                inputs = emptyMap()
                // Override language selection by uncommenting this and choosing your languages
                //inputs = mapOf("languages" to "go, javascript, csharp, python, cpp, java")
            )
        )
        // Autobuild attempts to build any compiled languages (C/C++, C#, or Java).
        // If this step fails, then you should remove it and run the build manually (see below).
        //uses(
        //    name = "Autobuild",
        //    action = CustomAction(
        //        actionOwner = "github",
        //        actionName = "codeql-action/autobuild",
        //        actionVersion = "v1",
        //        inputs = emptyMap()
        //    )
        //)
        //
        // ℹ️ Command-line programs to run using the OS shell.
        // 📚 https://git.io/JvXDl
        //
        // ✏️ If the Autobuild fails above, remove it and uncomment the following
        //    three lines and modify them (or add more) to build your code if your
        //    project uses a compiled language
        //
        //run(
        //    command = """
        //        make bootstrap
        //        make release
        //    """.trimIndent()
        //)

        // Manually added: build
        // we have to disable build cache for now as it seems to be necessary for the compiler to run during the build
        // https://docs.github.com/en/github/finding-security-vulnerabilities-and-errors-in-your-code/troubleshooting-the-codeql-workflow#no-code-found-during-the-build
        uses(
            name = "Build Spock Classes",
            action = GradleBuildActionV2(
                arguments = """--stacktrace --no-build-cache testClasses "-Dvariant=${expr("matrix.variant")}""""
            )
        )
        uses(
            name = "Perform CodeQL Analysis",
            action = CustomAction(
                actionOwner = "github",
                actionName = "codeql-action/analyze",
                actionVersion = "v3",
                inputs = emptyMap()
            )
        )
    }
}.writeToFile()
