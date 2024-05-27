import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.joetr.sync.sphere.root")
    alias(libs.plugins.compose.compiler)
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
            modules("java.naming")

            outputBaseDir.set(layout.buildDirectory.asFile.get().resolve("release"))

            targetFormats(TargetFormat.Dmg, TargetFormat.Pkg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Sync Sphere"
            packageVersion = "1.4.3"

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon/desktop_icon.icns"))
                bundleID = "com.joetr.sync.sphere.mac"
                signing {
                    sign.set(true)
                    identity.set("Joseph Roskopf")
                }
                notarization {
                    appleID.set("joebrothehobo@gmail.com")
                    password.set("@keychain:NOTARIZATION_PASSWORD")
                }
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

tasks.withType<JavaExec>().configureEach {
    systemProperty("syncSphereDebug", System.getProperty("syncSphereDebug"))
}
