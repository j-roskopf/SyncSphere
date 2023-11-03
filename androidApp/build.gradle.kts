plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.joetr.sync.sphere.root")
    id("com.joetr.sync.sphere.precommit")
    id("com.google.gms.google-services")
}

kotlin {
    android()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.koin)
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.joetr.sync.sphere"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.joetr.sync.sphere"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())

        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
    }
    packaging {
        resources.pickFirsts.add("META-INF/versions/9/previous-compilation-data.bin")
    }
    kotlin {
        jvmToolchain(libs.versions.jdk.get().toInt())
    }
}

dependencies {
    // todo joer
    implementation("com.google.firebase:firebase-common-ktx:20.3.3")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.9")
}
