package com.techshroom.inciseblue.lwjgl

import com.techshroom.inciseblue.InciseBlueExtension
import com.techshroom.inciseblue.ibExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class IBLwjglPlugin : Plugin<Project> {

    private fun Project.addLwjglJarDep(conf: String, version: String, name: String) {
        dependencies {
            conf(group = "org.lwjgl", name = getLwjglName(name), version = version)
        }
    }

    private fun Project.addLwjglNativeDep(conf: String, version: String, name: String) {
        dependencies {
            listOf("macos", "linux", "windows").forEach { platform ->
                conf(group = "org.lwjgl", name = getLwjglName(name), version = version, classifier = "natives-$platform")
            }
        }
    }

    private fun getLwjglName(name: String) =
            "lwjgl" + when {
                name.isBlank() -> ""
                else -> "-$name"
            }

    override fun apply(project: Project) {
        val cfg = project.ibExt.lwjgl

        project.afterEvaluate {
            cfg.validate()
            val compile = cfg.getCompileConfigurationFrom(project)
            val version = cfg.lwjglVersion!!
            cfg.dependencies.forEach {
                project.addLwjglJarDep(compile, version, it.name)
                if (it.natives) {
                    project.addLwjglNativeDep(compile, version, it.name)
                }
            }
        }
    }
}
