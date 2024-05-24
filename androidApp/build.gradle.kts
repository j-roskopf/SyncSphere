import java.io.FileInputStream
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.joetr.sync.sphere.root")
    id("com.joetr.sync.sphere.precommit")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.koin)
            }
        }
    }
}

val keyProperties = Properties()
val keyPropertiesFile = rootProject.file("androidApp/key.properties")
if (keyPropertiesFile.exists()) {
    keyProperties.load(FileInputStream(keyPropertiesFile))
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.joetr.sync.sphere"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.joetr.sync.sphere"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = (findProperty("android.versionCode") as String).toInt()
        versionName = findProperty("android.versionName") as String
    }

    signingConfigs {
        create("release") {
            keyAlias = keyProperties.getProperty("keyAlias")
            keyPassword = keyProperties.getProperty("keyPassword")
            storeFile = file(keyProperties.getProperty("storeFile") ?: "empty/file")
            storePassword = keyProperties.getProperty("storePassword")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
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
    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    implementation(libs.firebase.common.google)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.datetime)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

tasks.withType<JavaExec>().configureEach {
    systemProperty("SIGNING_KEY_PASSWORD", System.getProperty("SIGNING_KEY_PASSWORD"))
    systemProperty("SIGNING_KEY_ALIAS", System.getProperty("SIGNING_KEY_ALIAS"))
    systemProperty("SIGNING_STORE_PASSWORD", System.getProperty("SIGNING_STORE_PASSWORD"))
}
