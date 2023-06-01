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
import io.github.typesafegithub.workflows.actions.actions.CheckoutV3.FetchDepth
import io.github.typesafegithub.workflows.actions.codecov.CodecovActionV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.github
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.secrets
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
    sourceFile = __FILE__.toPath(),
    targetFileName = "${__FILE__.name.substringBeforeLast(".main.kts")}.yml"
) {
    val GITHUB_TOKEN by secrets
    val SONATYPE_OSS_USER by secrets
    val SONATYPE_OSS_PASSWORD by secrets
    val SIGNING_GPG_PASSWORD by secrets

    val buildAndVerify = job(
        id = "build-and-verify",
        name = "Build and Verify",
        runsOn = RunnerType.Custom(expr(Matrix.operatingSystem)),
        condition = "${github.repository} == 'spockframework/spock'",
        strategy = Strategy(
            matrix = Matrix.full
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
                    """"-DjavaVersion=${expr(Matrix.java)}"""",
                    "-Dscan.tag.main-build"
                ).joinToString(" ")
            ),
            env = commonCredentials
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
        runsOn = RunnerType.Custom(expr(Matrix.operatingSystem)),
        needs = listOf(buildAndVerify),
        strategyMatrix = mapOf(
            "os" to listOf("ubuntu-latest"),
            // publish needs to be done for all versions
            "variant" to Matrix.axes.variants,
            // publish needs the min supported java version
            "java" to Matrix.axes.javaVersions.take(1)
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3()
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr(Matrix.java)
            )
        )
        uses(
            name = "Publish Spock",
            action = GradleBuildActionV2(
                arguments = listOf(
                    "--no-parallel",
                    "--stacktrace",
                    "ghActionsPublish",
                    """"-Dvariant=${expr(Matrix.variant)}"""",
                    """"-DjavaVersion=${expr(Matrix.java)}"""",
                    "-Dscan.tag.main-publish"
                ).joinToString(" ")
            ),
            env = linkedMapOf(
                "GITHUB_TOKEN" to expr(GITHUB_TOKEN),
                "SONATYPE_OSS_USER" to expr(SONATYPE_OSS_USER),
                "SONATYPE_OSS_PASSWORD" to expr(SONATYPE_OSS_PASSWORD),
                "SIGNING_PASSWORD" to expr(SIGNING_GPG_PASSWORD)
            ).apply { putAll(commonCredentials) }
        )
    }
    job(
        id = "publish-release-docs",
        name = "Publish Release Docs",
        runsOn = RunnerType.Custom(expr(Matrix.operatingSystem)),
        needs = listOf(releaseSpock),
        strategyMatrix = mapOf(
            "os" to listOf("ubuntu-latest"),
            // docs need the highest variant
            "variant" to Matrix.axes.variants.takeLast(1),
            // docs need the highest java version
            "java" to Matrix.axes.javaVersions.takeLast(1)
        )
    ) {
        uses(
            name = "Checkout Repository",
            action = CheckoutV3()
        )
        uses(
            name = "Set up JDKs",
            action = SetupBuildEnv(
                additionalJavaVersion = expr(Matrix.java)
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
                    """"-Dvariant=${expr(Matrix.variant)}"""",
                    """"-DjavaVersion=${expr(Matrix.java)}"""",
                    "-Dscan.tag.main-docs"
                ).joinToString(" ")
            ),
            env = linkedMapOf(
                "GITHUB_TOKEN" to expr(GITHUB_TOKEN)
            ).apply { putAll(commonCredentials) }
        )
    }
}.writeToFile()
