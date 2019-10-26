package com.techshroom.inciseblue.jfx

import org.gradle.api.Project

open class JfxExtension {
    var compileConfiguration: String? = null
    var allNativesConfiguration: String = "javafxAllNatives"
    var jfxVersion: String? = null
    var dependencies: MutableList<String> = mutableListOf()

    fun getCompileConfigurationFrom(project: Project): String {
        return compileConfiguration
            ?: listOf("api", "implementation", "compile")
                .firstOrNull { project.configurations.findByName(it) != null }
            ?: throw IllegalStateException("No compile configurations available")
    }

    fun addDependency(name: String) {
        dependencies.add(name)
    }

    fun validate() {
        requireNotNull(jfxVersion) {
            "JavaFX version must be set (inciseBlue.jfx.jfxVersion)"
        }
    }
}
