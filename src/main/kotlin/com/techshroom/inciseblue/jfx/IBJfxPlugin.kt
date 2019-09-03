package com.techshroom.inciseblue.jfx

import com.techshroom.inciseblue.ibExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class IBJfxPlugin : Plugin<Project> {

    private fun Project.addJfxJarDep(conf: String, version: String, name: String) {
        dependencies {
            conf(group = "org.openjfx", name = getJfxName(name), version = version)
        }
    }

    private fun Project.addJfxNativeDep(conf: String, version: String, name: String) {
        dependencies {
            listOf("mac", "linux", "win").forEach { platform ->
                conf(group = "org.openjfx", name = getJfxName(name), version = version, classifier = platform)
            }
        }
    }

    private fun getJfxName(name: String) =
        "javafx" + when {
            name.isBlank() -> ""
            else -> "-$name"
        }

    override fun apply(project: Project) {
        val cfg = project.ibExt.jfx

        project.afterEvaluate {
            cfg.validate()
            val compile = cfg.getCompileConfigurationFrom(project)
            val version = cfg.jfxVersion!!
            cfg.dependencies.forEach {
                project.addJfxJarDep(compile, version, it)
                project.addJfxNativeDep(compile, version, it)
            }
        }
    }
}
