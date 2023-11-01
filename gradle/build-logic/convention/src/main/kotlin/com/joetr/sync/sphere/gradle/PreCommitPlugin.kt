package com.joetr.sync.sphere.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.io.File

class PreCommitPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val preCommitScriptFileLocation = "${target.rootDir}/pre-commit.sh"

            // no pre-commit script defined, return
            if (file(preCommitScriptFileLocation).exists().not()) return

            tasks.register("installFormattingPreCommitHook") {
                val baseGitHooksDirectory =
                    "${target.rootDir}${File.separator}.git${File.separator}hooks"

                // make sure it git/hooks exists
                file(baseGitHooksDirectory).mkdirs()

                // define outputs of this task so that it is not executed every time
                val gitHookPreCommitFile = file("$baseGitHooksDirectory${File.separator}pre-commit")
                outputs.file(gitHookPreCommitFile.absolutePath)
                val os = DefaultNativePlatform.getCurrentOperatingSystem()
                when {
                    // currently windows is not supported
                    os.isLinux || os.isMacOsX -> {
                        // symlink bash script
                        doLast {
                            exec { commandLine("chmod", "+x", preCommitScriptFileLocation) }
                            exec {
                                commandLine(
                                    "ln",
                                    "-s",
                                    "-f",
                                    preCommitScriptFileLocation,
                                    gitHookPreCommitFile,
                                )
                            }
                            exec { commandLine("chmod", "+x", gitHookPreCommitFile) }
                        }
                    }
                }
            }

            // we do not want to install the pre-commit hook on CI
            if (System.getenv("Build.Reason") == null) {
                tasks.getByPath(":${target.name}:preBuild")
                    .dependsOn("installFormattingPreCommitHook")
            }
        }
    }
}
