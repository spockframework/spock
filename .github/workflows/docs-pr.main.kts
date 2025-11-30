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

@file:Import("common.main.kts")

@file:Repository("https://repo.maven.apache.org/maven2/")
// work-around for https://youtrack.jetbrains.com/issue/KT-69145
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.6.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout___major:[v5,v6-alpha)")
@file:DependsOn("actions:upload-artifact___major:[v5,v6-alpha)")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.Checkout.FetchDepth
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.MergeGroup
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.github
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Verify Docs",
    on = listOf(
        Push(
            branchesIgnore = listOf(
                "master",
                "gh-pages"
            )
        ),
        PullRequest(),
        MergeGroup()
    ),
    sourceFile = __FILE__,
    // https://stackoverflow.com/a/72408109/16358266
    concurrency = Concurrency(
        group = "${expr { github.workflow }}-${expr("${github.eventPullRequest.pull_request.number} || ${github.ref}")}",
        cancelInProgress = true
    )
) {
    job(
        id = "docs-and-javadoc",
        name = "Docs and JavaDoc",
        runsOn = UbuntuLatest,
    ) {
        uses(
            name = "Checkout Repository",
            action = Checkout(
                fetchDepth = FetchDepth.Value(1)
            )
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = Matrix.axes.javaVersions.last()
            )
        )
        run(
            name = "Install GraphViz",
            command = "sudo apt update && sudo apt install --yes graphviz"
        )
        run(
            name = "Build Docs",
            command = listOf(
                "./gradlew",
                "--stacktrace",
                "asciidoctor",
                "javadoc",
                """"-Dvariant=${Matrix.axes.variants.last()}"""",
                """"-DjavaVersion=${Matrix.axes.javaVersions.last()}""""
            ).joinToString(" ")
        )
        uses(
            name = "Archive and upload docs",
            action = UploadArtifact(
                name = "docs",
                path = listOf(
                    "build/docs/**",
                    "build/javadoc/**"
                )
            )
        )
    }
}
