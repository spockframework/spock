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

import io.github.typesafegithub.workflows.actions.actions.CheckoutV3
import io.github.typesafegithub.workflows.actions.codecov.CodecovActionV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.github
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

workflow(
    name = "Verify Branches and PRs",
    on = listOf(
        Push(
            branchesIgnore = listOf(
                "master",
                "gh-pages"
            )
        ),
        PullRequest()
    ),
    sourceFile = __FILE__.toPath(),
    targetFileName = "${__FILE__.name.substringBeforeLast(".main.kts")}.yml",
    // https://stackoverflow.com/a/72408109/16358266
    concurrency = Concurrency(
        group = "${expr { github.workflow }}-${expr("${github.eventPullRequest.pull_request.number} || ${github.ref}")}",
        cancelInProgress = true
    )
) {
    job(
        id = "check_all_workflow_yaml_consistency",
        name = "Check all Workflow YAML consistency",
        runsOn = UbuntuLatest
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3()
        )
        run(
            name = "Regenerate all workflow YAMLs and check for modifications",
            command = """find .github/workflows -mindepth 1 -maxdepth 1 -name "*.main.kts" | xargs -ri sh -c '{} && git diff --exit-code'"""
        )
    }

    job(
        id = "build-and-verify",
        name = "Build and Verify",
        runsOn = RunnerType.Custom(expr(Matrix.operatingSystem)),
        strategy = Strategy(
            matrix = Matrix.full
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3(
                // Codecov needs fetch-depth > 1
                fetchDepth = CheckoutV3.FetchDepth.Value(2)
            )
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr(Matrix.java)
            )
        )
        uses(
            name = "Build Spock",
            action = GradleBuildActionV2(
                arguments = listOf(
                    "--no-parallel",
                    "--stacktrace",
                    "ghActionsBuild",
                    """"-Dvariant=${expr(Matrix.variant)}"""",
                    """"-DjavaVersion=${expr(Matrix.java)}""""
                ).joinToString(" ")
            ),
            // secrets are not injected for pull requests
            env = commonCredentials
        )
        uses(
            name = "Upload to Codecov.io",
            action = CodecovActionV3()
        )
    }
}.writeToFile()
