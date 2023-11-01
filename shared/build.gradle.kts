plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    // re-enable after kotlin 1.9 support https://github.com/icerockdev/moko-resources/issues/535
    // also re-add back the script in project.pbxproj from the commit this was added
    // id("dev.icerock.mobile.multiplatform-resources")
    id("com.joetr.sync.sphere.root")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
            // todo joer move to toml
            export("com.mohamedrejeb.calf:calf-ui:0.2.0")
        }
        // re-enable after kotlin 1.9 support https://github.com/icerockdev/moko-resources/issues/535
        // extraSpecAttributes["resources"] = "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
        extraSpecAttributes["exclude_files"] = "['src/commonMain/resources/MR/**']"
    }

    sourceSets {
        // re-enable after kotlin 1.9 support https://github.com/icerockdev/moko-resources/issues/535
        // val mokoResourcesVersion = extra["moko.resources.version"] as String

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // re-enable after kotlin 1.9 support https://github.com/icerockdev/moko-resources/issues/535
                // implementation("dev.icerock.moko:resources-compose:$mokoResourcesVersion")

                // fix of Could not find "shared/build/kotlinTransformedMetadataLibraries/commonMain/org.jetbrains.kotlinx-atomicfu-0.17.3-nativeInterop-8G5yng.klib"
                implementation("org.jetbrains.kotlinx:atomicfu:0.17.3")

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

                implementation("dev.gitlive:firebase-firestore:1.10.3")
                implementation("dev.gitlive:firebase-common:1.10.3")
                // has to be API to export to separate configurations
                api("com.mohamedrejeb.calf:calf-ui:0.2.0")
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.0")
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
                api("androidx.activity:activity-compose:1.6.1")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.9.0")
                implementation(libs.ktor.client.okhttp)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

// re-enable after kotlin 1.9 support https://github.com/icerockdev/moko-resources/issues/535
// multiplatformResources {
//    multiplatformResourcesPackage = "com.joetr.sync.sphere.common"
// }

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.joetr.sync.sphere.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    sourceSets["main"].resources.exclude("src/commonMain/resources/MR")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
