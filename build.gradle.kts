plugins {
    id("com.joetr.sync.sphere.root")
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    id("com.android.application").version(libs.versions.agp).apply(false)
    id("com.android.library").version(libs.versions.agp).apply(false)
    id("org.jetbrains.compose").version(libs.versions.compose).apply(false)
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("io.gitlab.arturbosch.detekt").version(libs.versions.detektGradlePlugin)
    id("com.google.gms.google-services").version("4.3.14").apply(false)
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

subprojects {
    apply(from = "$rootDir/buildscripts/detekt.gradle")
}

tasks {
    val detektAll by registering(io.gitlab.arturbosch.detekt.Detekt::class) {
        parallel = true
        setSource(files(projectDir))
        include("**/*.kt")
        include("**/*.kts")
        exclude("**/resources/**")
        exclude("**/build/**")
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        baseline.set(file("$rootDir/config/detekt/baseline.xml"))
        buildUponDefaultConfig = false
    }

    val detektGenerateBaseline by registering(io.gitlab.arturbosch.detekt.DetektCreateBaselineTask::class) {
        baseline.set(file("$rootDir/config/detekt/baseline.xml"))
        setSource(files(projectDir))
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        include("**/*.kt")
        include("**/*.kts")
        exclude("**/resources/**", "**/build/**")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(true)

        if (project.hasProperty("enableComposeCompilerReports")) {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    project.buildDir.absolutePath + "/compose_metrics",
            )
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    project.buildDir.absolutePath + "/compose_metrics",
            )
        }
    }
}
