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

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:0.41.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV3
import io.github.typesafegithub.workflows.actions.actions.CheckoutV3.FetchDepth
import io.github.typesafegithub.workflows.actions.codecov.CodecovActionV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.actions.Action
import io.github.typesafegithub.workflows.domain.actions.LocalAction
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.github
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

workflow(
    name = "Build and Release Spock",
    on = listOf(
        Push(
            branches = listOf("master"),
            tags = listOf("spock-*")
        )
    ),
    sourceFile = __FILE__.toPath()
) {
    val GITHUB_TOKEN by Contexts.secrets
    val SONATYPE_OSS_USER by Contexts.secrets
    val SONATYPE_OSS_PASSWORD by Contexts.secrets
    val SIGNING_GPG_PASSWORD by Contexts.secrets
    val SPOCK_BUILD_CACHE_USERNAME by Contexts.secrets
    val SPOCK_BUILD_CACHE_PASSWORD by Contexts.secrets
    val GRADLE_ENTERPRISE_ACCESS_KEY by Contexts.secrets

    val buildAndVerify = job(
        id = "build-and-verify",
        name = "Build and Verify",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        condition = "${github.repository} == 'spockframework/spock'",
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
                fetchDepth = FetchDepth.Value(2)
            )
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr("matrix.java")
            )
        )
        uses(
            name = "Build Spock",
            action = GradleBuildActionV2(
                arguments = listOf(
                    "--no-parallel",
                    "--stacktrace",
                    "ghActionsBuild",
                    """"-Dvariant=${expr("matrix.variant")}"""",
                    """"-DjavaVersion=${expr("matrix.java")}"""",
                    "-Dscan.tag.main-build"
                ).joinToString(" ")
            ),
            env = linkedMapOf(
                "ORG_GRADLE_PROJECT_spockBuildCacheUsername" to expr(SPOCK_BUILD_CACHE_USERNAME),
                "ORG_GRADLE_PROJECT_spockBuildCachePassword" to expr(SPOCK_BUILD_CACHE_PASSWORD),
                "GRADLE_ENTERPRISE_ACCESS_KEY" to expr(GRADLE_ENTERPRISE_ACCESS_KEY)
            )
        )
        run(
            name = "Stop Daemon",
            command = "./gradlew --stop"
        )
        uses(
            name = "Upload to Codecov.io",
            action = CodecovActionV3()
        )
    }
    val releaseSpock = job(
        id = "release-spock",
        name = "Release Spock",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        needs = listOf(buildAndVerify),
        strategyMatrix = mapOf(
            "os" to listOf("ubuntu-latest"),
            // publish needs to be done for all versions
            "variant" to listOf("2.5", "3.0", "4.0"),
            // publish needs the min supported java version
            "java" to listOf("8")
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3()
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr("matrix.java")
            )
        )
        uses(
            name = "Publish Spock",
            action = GradleBuildActionV2(
                arguments = listOf(
                    "--no-parallel",
                    "--stacktrace",
                    "ghActionsPublish",
                    """"-Dvariant=${expr("matrix.variant")}"""",
                    """"-DjavaVersion=${expr("matrix.java")}"""",
                    "-Dscan.tag.main-publish"
                ).joinToString(" ")
            ),
            env = linkedMapOf(
                "GITHUB_TOKEN" to expr(GITHUB_TOKEN),
                "SONATYPE_OSS_USER" to expr(SONATYPE_OSS_USER),
                "SONATYPE_OSS_PASSWORD" to expr(SONATYPE_OSS_PASSWORD),
                "SIGNING_PASSWORD" to expr(SIGNING_GPG_PASSWORD),
                "ORG_GRADLE_PROJECT_spockBuildCacheUsername" to expr(SPOCK_BUILD_CACHE_USERNAME),
                "ORG_GRADLE_PROJECT_spockBuildCachePassword" to expr(SPOCK_BUILD_CACHE_PASSWORD),
                "GRADLE_ENTERPRISE_ACCESS_KEY" to expr(GRADLE_ENTERPRISE_ACCESS_KEY)
            )
        )
    }
    job(
        id = "publish-release-docs",
        name = "Publish Release Docs",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        needs = listOf(releaseSpock),
        strategyMatrix = mapOf(
            "os" to listOf("ubuntu-latest"),
            // docs need the highest variant
            "variant" to listOf("4.0"),
            // docs need the highest java version
            "java" to listOf("17")
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3()
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr("matrix.java")
            )
        )
        run(
            name = "Create Temporary Branch",
            command = "git checkout -b \"docs-\$GITHUB_SHA\""
        )
        uses(
            name = "Publish Docs",
            action = GradleBuildActionV2(
                arguments = listOf(
                    "--no-parallel",
                    "--stacktrace",
                    "ghActionsDocs",
                    """"-Dvariant=${expr("matrix.variant")}"""",
                    """"-DjavaVersion=${expr("matrix.java")}"""",
                    "-Dscan.tag.main-docs"
                ).joinToString(" ")
            ),
            env = linkedMapOf(
                "GITHUB_TOKEN" to expr(GITHUB_TOKEN),
                "ORG_GRADLE_PROJECT_spockBuildCacheUsername" to expr(SPOCK_BUILD_CACHE_USERNAME),
                "ORG_GRADLE_PROJECT_spockBuildCachePassword" to expr(SPOCK_BUILD_CACHE_PASSWORD),
                "GRADLE_ENTERPRISE_ACCESS_KEY" to expr(GRADLE_ENTERPRISE_ACCESS_KEY)
            )
        )
    }
}.writeToFile()

data class SetupBuildEnv(
    val additionalJavaVersion: String? = null
) : LocalAction<Action.Outputs>("./.github/actions/setup-build-env") {
    override fun toYamlArguments() =
        additionalJavaVersion?.let { linkedMapOf("additional-java-version" to it) } ?: linkedMapOf()

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)
}
