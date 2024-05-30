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

        val isAppStoreRelease = project.property("macOsAppStoreRelease").toString().toBoolean()

        nativeDistributions {
            modules("java.naming")

            outputBaseDir.set(layout.buildDirectory.asFile.get().resolve("release"))

            nativeDistributions {
                if (isAppStoreRelease) {
                    appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
                }
                modules("java.sql")
            }

            targetFormats(TargetFormat.Dmg, TargetFormat.Pkg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Sync Sphere"
            packageVersion = "1.5.4"

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon/desktop_icon.icns"))
                bundleID = "com.joetr.sync.sphere.mac"

                appStore = isAppStoreRelease

                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }

                signing {
                    sign.set(true)
                    identity.set("Joseph Roskopf")
                }

                notarization {
                    appleID.set("joebrothehobo@gmail.com")
                    password.set("@keychain:NOTARIZATION_PASSWORD")
                }

                minimumSystemVersion = "12.0"

                if (isAppStoreRelease) {
                    entitlementsFile.set(project.file("entitlements.plist"))
                    runtimeEntitlementsFile.set(project.file("runtime-entitlements.plist"))
                    provisioningProfile.set(project.file("embedded.provisionprofile"))
                    runtimeProvisioningProfile.set(project.file("runtime.provisionprofile"))
                } else {
                    entitlementsFile.set(project.file("default.entitlements"))
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

val macExtraPlistKeys: String
    get() = """
        <key>ITSAppUsesNonExemptEncryption</key>
        <false/>
    """.trimIndent()
