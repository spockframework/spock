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
@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("gradle:wrapper-validation-action:v3")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.gradle.WrapperValidationAction
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.MergeGroup
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Validate Gradle Wrapper",
    on = listOf(
        Push(),
        PullRequest(),
        MergeGroup()
    ),
    sourceFile = __FILE__,
    targetFileName = "${__FILE__.name.substringBeforeLast(".main.kts")}.yml"
) {
    job(
        id = "validation",
        name = "Validation",
        runsOn = UbuntuLatest
    ) {
        uses(
            name = "Checkout Repository",
            action = Checkout()
        )
        uses(
            name = "Validate Wrapper",
            action = WrapperValidationAction()
        )
    }
}
