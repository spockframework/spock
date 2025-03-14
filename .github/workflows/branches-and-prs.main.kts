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
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.3.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout___major:[v4,v5-alpha)")
@file:DependsOn("codecov:codecov-action___major:[v5,v6-alpha)")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.Checkout.FetchDepth
import io.github.typesafegithub.workflows.actions.codecov.CodecovAction
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.MergeGroup
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.github
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Verify Branches and PRs",
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
        id = "check_all_workflow_yaml_consistency",
        name = "Check all Workflow YAML Consistency",
        runsOn = UbuntuLatest
    ) {
        uses(
            name = "Checkout Repository",
            action = Checkout()
        )
        run(
            name = "Regenerate all Workflow YAMLs",
            command = """find .github/workflows -mindepth 1 -maxdepth 1 -name '*.main.kts' -exec {} \;"""
        )
        run(
            name = "Check for Modifications",
            command = """
                git add --intent-to-add .
                git diff --exit-code
            """.trimIndent()
        )
    }

    val matrix = Matrix.full
    with(__FILE__.parentFile.resolve("../codecov.yml")) {
        readText()
            .replace("after_n_builds:.*+$".toRegex(), "after_n_builds: ${matrix.size}")
            .let(::writeText)
    }
    job(
        id = "build-and-verify",
        name = "Build and Verify",
        runsOn = RunnerType.Custom(expr(Matrix.operatingSystem)),
        strategy = Strategy(
            matrix = matrix
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = Checkout(
                // Codecov needs fetch-depth > 1
                fetchDepth = FetchDepth.Value(2)
            )
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr(Matrix.javaVersion)
            )
        )
        run(
            name = "Build Spock",
            command = listOf(
                "./gradlew",
                "--stacktrace",
                "ghActionsBuild",
                """"-Dvariant=${expr(Matrix.variant)}"""",
                """"-DjavaVersion=${expr(Matrix.javaVersion)}""""
            ).joinToString(" "),
            // secrets are not injected for pull requests
            env = commonCredentials
        )
        uses(
            name = "Upload to Codecov.io",
            action = CodecovAction(
                failCiIfError = true
            )
        )
    }
}
