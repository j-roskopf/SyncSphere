import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.joetr.sync.sphere.root")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.koin)
                implementation(libs.kotlinx.coroutines.swing)

                implementation(project(":shared"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Sync Sphere"
            packageVersion = "1.1.1"

            // todo joer make platform icons alignec with platforms
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon/desktop_icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon/desktop_icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon/desktop_icon.png"))
            }
        }
    }
}
