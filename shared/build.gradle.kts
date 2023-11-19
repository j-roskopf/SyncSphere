plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.joetr.sync.sphere.root")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("co.touchlab.crashkios.crashlyticslink") version "0.8.5"
}

kotlin {
    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries {
            framework {
                baseName = "shared"
                isStatic = true
            }
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.koin)
                implementation(libs.voyager.tabNavigator)
                implementation(libs.koin)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx)
                implementation(libs.ktor.client.logging)

                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.calendar.compose.datepicker)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
                implementation(libs.coroutinesTest)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.activity.compose)
                api(libs.appcompat)
                api(libs.core.ktx)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val mobileMain by creating {
            androidMain.dependsOn(this)
            iosMain.dependsOn(this)
            dependencies {
                dependsOn(commonMain)

                implementation(libs.firebase.firestore)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.crashlytics)
                implementation(libs.crashlytics)
                implementation(libs.firebase.common)
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.joetr.sync.sphere.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
    }
    kotlin {
        jvmToolchain(libs.versions.jdk.get().toInt())
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
}
