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
@file:DependsOn("org.codehaus.groovy:groovy:3.0.15")

import groovy.lang.Binding
import groovy.lang.GroovyShell
import io.github.typesafegithub.workflows.actions.actions.CheckoutV3
import io.github.typesafegithub.workflows.actions.codecov.CodecovActionV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.actions.Action.Outputs
import io.github.typesafegithub.workflows.domain.actions.LocalAction
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
    targetFileName = "${__FILE__.name.substringBeforeLast(".main.kts")}.yml",
    // https://stackoverflow.com/a/72408109/16358266
    concurrency = Concurrency(
        group = "${expr { github.workflow }}-${expr("${Contexts.github.eventPullRequest.pull_request.number} || ${Contexts.github.ref}")}",
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

    val (javaVersions, variants) = getMatrixAxes()
    job(
        id = "build-and-verify",
        name = "Build and Verify",
        runsOn = RunnerType.Custom(expr("matrix.os")),
        _customArguments = mapOf(
            "strategy" to mapOf(
                "fail-fast" to false,
                "matrix" to mapOf(
                    "os" to listOf("ubuntu-latest"),
                    "variant" to variants,
                    "java" to javaVersions,
                    "exclude" to javaVersions
                        .filter { it.toInt() >= 17 }
                        .map { javaVersion ->
                            mapOf(
                                "os" to "ubuntu-latest",
                                "variant" to "2.5",
                                "java" to javaVersion
                            )
                        },
                    "include" to listOf("windows-latest", "macos-latest")
                        .flatMap { os -> variants.map { os to it } }
                        .map { (os, variant) ->
                            mapOf(
                                "os" to os,
                                "variant" to variant,
                                "java" to javaVersions.first()
                            )
                        }
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
            action = SetupBuildEnv(
                additionalJavaVersion = expr("matrix.java")
            )
        )
        val SPOCK_BUILD_CACHE_USERNAME by Contexts.secrets
        val SPOCK_BUILD_CACHE_PASSWORD by Contexts.secrets
        val GRADLE_ENTERPRISE_ACCESS_KEY by Contexts.secrets
        uses(
            name = "Build Spock",
            action = GradleBuildActionV2(
                arguments = listOf(
                    "--no-parallel",
                    "--stacktrace",
                    "ghActionsBuild",
                    """"-Dvariant=${expr("matrix.variant")}"""",
                    """"-DjavaVersion=${expr("matrix.java")}""""
                ).joinToString(" ")
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

data class SetupBuildEnv(
    val additionalJavaVersion: String? = null
) : LocalAction<Outputs>("./.github/actions/setup-build-env") {
    override fun toYamlArguments() =
        additionalJavaVersion?.let { linkedMapOf("additional-java-version" to it) } ?: linkedMapOf()

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)
}

fun getMatrixAxes(): Pair<List<String>, List<String>> {
    val binding = object : Binding() {
        lateinit var javaVersions: List<String>
        lateinit var variants: List<String>

        override fun setVariable(name: String?, value: Any?) {
            when (name) {
                "javaVersions" -> javaVersions = (value as List<Int>).map { it.toString() }
                "variants" -> variants = value as List<String>
            }
        }
    }
    GroovyShell(binding).evaluate(__FILE__.parentFile.resolve("../../matrix.groovy"))
    return binding.javaVersions to binding.variants
}
