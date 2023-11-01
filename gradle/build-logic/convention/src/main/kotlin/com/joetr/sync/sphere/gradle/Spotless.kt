package com.joetr.sync.sphere.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureSpotless() {
    val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion

    with(pluginManager) {
        apply("com.diffplug.spotless")
    }

    spotless {
        kotlin {
            target("src/*/kotlin/**/*.kt", "src/*/java/**/*.kt")
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
        format("misc") {
            target("*.md", ".gitignore", "*.xml", "*.gradle")
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

private fun Project.spotless(action: SpotlessExtension.() -> Unit) = extensions.configure<SpotlessExtension>(action)
