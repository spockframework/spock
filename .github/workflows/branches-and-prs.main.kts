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
import io.github.typesafegithub.workflows.actions.codecov.CodecovActionV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
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
    //# https://stackoverflow.com/a/72408109/16358266
    concurrency = Concurrency(
        group = "${expr { github.workflow }}-${expr("${Contexts.github.eventPullRequest.pull_request.number} || ${Contexts.github.ref}")}",
        cancelInProgress = true
    )
) {
    job(
        id = "build-and-verify",
        name = "Build and Verify",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        _customArguments = mapOf(
            "strategy" to mapOf(
                "fail-fast" to false,
                "matrix" to mapOf(
                    "os" to listOf("ubuntu-latest"),
                    "variant" to listOf("2.5", "3.0", "4.0"),
                    "java" to listOf("8", "11", "17"),
                    "exclude" to listOf(
                        mapOf(
                            "os" to "ubuntu-latest",
                            "variant" to "2.5",
                            "java" to "17"
                        )
                    ),
                    "include" to listOf(
                        mapOf(
                            "os" to "windows-latest",
                            "variant" to "2.5",
                            "java" to "8"
                        ),
                        mapOf(
                            "os" to "windows-latest",
                            "variant" to "3.0",
                            "java" to "8"
                        ),
                        mapOf(
                            "os" to "windows-latest",
                            "variant" to "4.0",
                            "java" to "8"
                        ),
                        mapOf(
                            "os" to "macos-latest",
                            "variant" to "2.5",
                            "java" to "8"
                        ),
                        mapOf(
                            "os" to "macos-latest",
                            "variant" to "3.0",
                            "java" to "8"
                        ),
                        mapOf(
                            "os" to "macos-latest",
                            "variant" to "4.0",
                            "java" to "8"
                        )
                    )
                )
            )
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
            action = CustomAction(
                actionOwner = ".github",
                actionName = "actions/setup-build-env",
                actionVersion = "v0",
                inputs = mapOf(
                    "additional-java-version" to expr("matrix.java")
                )
            ),
            _customArguments = mapOf(
                "uses" to "./.github/actions/setup-build-env"
            )
        )
        val SPOCK_BUILD_CACHE_USERNAME by Contexts.secrets
        val SPOCK_BUILD_CACHE_PASSWORD by Contexts.secrets
        val GRADLE_ENTERPRISE_ACCESS_KEY by Contexts.secrets
        uses(
            name = "Build Spock",
            action = GradleBuildActionV2(
                arguments = """--no-parallel --stacktrace ghActionsBuild "-Dvariant=${expr("matrix.variant")}" "-DjavaVersion=${
                    expr(
                        "matrix.java"
                    )
                }""""
            ),
            // secrets are not injected for pull requests
            env = linkedMapOf(
                "ORG_GRADLE_PROJECT_spockBuildCacheUsername" to expr(SPOCK_BUILD_CACHE_USERNAME),
                "ORG_GRADLE_PROJECT_spockBuildCachePassword" to expr(SPOCK_BUILD_CACHE_PASSWORD),
                "GRADLE_ENTERPRISE_ACCESS_KEY" to expr(GRADLE_ENTERPRISE_ACCESS_KEY)
            )
        )
        uses(
            name = "Upload to Codecov.io",
            action = CodecovActionV3()
        )
    }
}.writeToFile()
