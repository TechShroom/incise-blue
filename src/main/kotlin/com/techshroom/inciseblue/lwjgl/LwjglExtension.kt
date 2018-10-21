package com.techshroom.inciseblue.lwjgl

import org.gradle.api.Project

open class LwjglExtension {
    var compileConfiguration: String? = null
    var lwjglVersion: String? = null
    var dependencies: MutableList<LwjglDep> = mutableListOf()

    fun getCompileConfigurationFrom(project: Project): String {
        var confName = compileConfiguration
        if (confName == null) {
            // Use library API if not provided
            if (project.configurations.findByName("api") != null) {
                confName = "api"
            } else if (project.configurations.findByName("implementation") != null) {
                confName = "implementation"
            } else {
                confName = "compile"
            }
        }
        return confName
    }

    fun addDependency(dep: LwjglDep) {
        dependencies.add(dep)
    }

    fun addDependency(name: String, natives: Boolean = true) {
        addDependency(LwjglDep(name, natives))
    }

    fun validate() {
        if (lwjglVersion == null) {
            throw IllegalArgumentException("LWJGL version must be set (inciseBlue.lwjgl.lwjglVersion)");
        }
    }
}
