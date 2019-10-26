package com.techshroom.inciseblue.jfx

import com.google.gradle.osdetector.OsDetector
import com.google.gradle.osdetector.OsDetectorPlugin
import com.techshroom.inciseblue.ibExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.plugin
import org.gradle.kotlin.dsl.the

class IBJfxPlugin : Plugin<Project> {

    private fun Project.addJfxJarDep(conf: String, version: String, name: String) {
        dependencies {
            conf(group = "org.openjfx", name = getJfxName(name), version = version)
        }
    }

    private fun Project.addJfxNativeDep(
        conf: String, version: String, name: String, all: Boolean = false
    ) {
        val detectedOs = if (all) null else when (val os = the<OsDetector>().os) {
            "osx" -> "mac"
            "linux" -> "linux"
            "windows" -> "win"
            else -> throw IllegalStateException("No known natives for $os")
        }
        dependencies {
            listOf("mac", "linux", "win")
                .filter { all || it == detectedOs }
                .forEach { platform ->
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
        project.apply {
            plugin<OsDetectorPlugin>()
        }
        val cfg = project.ibExt.jfx

        project.afterEvaluate {
            cfg.validate()
            val compile = cfg.getCompileConfigurationFrom(project)
            val allNatives = cfg.allNativesConfiguration
            project.configurations.maybeCreate(allNatives)
            val version = cfg.jfxVersion!!
            cfg.dependencies.forEach {
                project.addJfxJarDep(compile, version, it)
                project.addJfxNativeDep(compile, version, it)
                project.addJfxNativeDep(allNatives, version, it, all = true)
            }
        }
    }
}
