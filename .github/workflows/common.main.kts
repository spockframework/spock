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

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")

import io.github.typesafegithub.workflows.domain.Job
import io.github.typesafegithub.workflows.domain.JobOutputs.EMPTY
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.actions.Action.Outputs
import io.github.typesafegithub.workflows.domain.actions.LocalAction
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.WorkflowBuilder
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.secrets
import io.github.typesafegithub.workflows.dsl.expressions.expr
import java.util.Properties

val GRADLE_ENTERPRISE_ACCESS_KEY by secrets

val commonCredentials = mapOf(
    "DEVELOCITY_ACCESS_KEY" to expr(GRADLE_ENTERPRISE_ACCESS_KEY)
)

data class Strategy(
    val failFast: Boolean? = false,
    val matrix: Matrix? = null
) {
    fun toCustomArguments() = mapOf(
        *listOfNotNull(
            failFast?.let { "fail-fast" to failFast },
            matrix?.let { "matrix" to matrix.toCustomArguments() }
        ).toTypedArray()
    )
}

data class Matrix(
    val operatingSystems: List<String>? = null,
    val variants: List<String>? = null,
    val javaVersions: List<String>? = null,
    val exclude: (Element.() -> Boolean)? = null,
    val includes: List<Element>? = null
) {
    val size by lazy {
        originalElements
            .filterNot(exclude ?: { true })
            .size +
            (includes?.size ?: 0)
    }

    private val originalElements by lazy {
        (operatingSystems ?: listOf(null))
            .map { Element(operatingSystem = it) }
            .flatMap { element -> (variants ?: listOf(null)).map { element.copy(variant = it) } }
            .flatMap { element -> (javaVersions ?: listOf(null)).map { element.copy(javaVersion = it) } }
    }

    fun toCustomArguments() = mapOf(
        *listOfNotNull(
            variants?.let { "variant" to variants },
            javaVersions?.let { "java" to javaVersions },
            operatingSystems?.let { "os" to operatingSystems },
            exclude?.let {
                "exclude" to originalElements
                    .filter(exclude)
                    .map { it.toCustomArguments() }
            },
            includes?.let { "include" to includes.map { it.toCustomArguments() } }
        ).toTypedArray()
    )

    data class Axes(
        val javaVersions: List<String>,
        val additionalJavaTestVersions: List<String>,
        val variants: List<String>
    )

    data class Element(
        val operatingSystem: String? = null,
        val variant: String? = null,
        val javaVersion: String? = null
    ) {
        fun toCustomArguments() = mapOf(
            *listOfNotNull(
                variant?.let { "variant" to variant },
                javaVersion?.let { "java" to javaVersion },
                operatingSystem?.let { "os" to operatingSystem }
            ).toTypedArray()
        )
    }

    companion object {
        val operatingSystem = "matrix.os"
        val variant = "matrix.variant"
        val javaVersion = "matrix.java"
    }
}

fun WorkflowBuilder.job(
    id: String,
    name: String? = null,
    runsOn: RunnerType,
    needs: List<Job<*>> = emptyList(),
    condition: String? = null,
    strategy: Strategy? = null,
    simpleStrategy: Map<String, List<String>>? = null,
    block: JobBuilder<EMPTY>.() -> Unit
): Job<EMPTY> = job(
    id = id,
    name = name,
    runsOn = runsOn,
    needs = needs,
    condition = condition,
    strategyMatrix = simpleStrategy,
    _customArguments = mapOf(
        *listOfNotNull(
            strategy?.let { "strategy" to strategy.toCustomArguments() }
        ).toTypedArray()
    ),
    block = block
)

val Matrix.Companion.full
    get() = Matrix(
        operatingSystems = listOf("ubuntu-latest"),
        variants = axes.variants,
        javaVersions = axes.javaVersions + axes.additionalJavaTestVersions,
        exclude = {
            ((variant == "2.5") && (javaVersion!!.toInt() >= 17)) ||
                ((variant == "5.0") && (javaVersion!!.toInt() < 11))
        },
        includes = listOf("windows-latest", "macos-latest")
            .map { Matrix.Element(operatingSystem = it) }
            .flatMap { element ->
                axes.variants.map {
                    element.copy(
                        variant = it,
                        javaVersion = if (it == "5.0") "11" else axes.javaVersions.first()
                    )
                }
            }
    )

val Matrix.Companion.axes by lazy {
    Properties().let { properties ->
        __FILE__
            .parentFile
            .resolve("../../gradle.properties")
            .inputStream()
            .use { properties.load(it) }

        Matrix.Axes(
            properties.getList("javaVersionsList"),
            properties.getList("additionalJavaTestVersionsList"),
            properties.getList("variantsList")
        )
    }
}

fun Properties.getList(key: String) =
    getProperty(key)
        ?.trim()
        ?.split("""\s*+,\s*+""".toRegex())
        ?.filter { it.isNotBlank() }
        ?: emptyList()

data class SetupBuildEnv(
    val additionalJavaVersion: String? = null
) : LocalAction<Outputs>("./.github/actions/setup-build-env") {
    override fun toYamlArguments() =
        additionalJavaVersion
            ?.let { linkedMapOf("additional-java-version" to it) }
            ?: linkedMapOf()

    override fun buildOutputObject(stepId: String): Outputs = Outputs(stepId)
}
