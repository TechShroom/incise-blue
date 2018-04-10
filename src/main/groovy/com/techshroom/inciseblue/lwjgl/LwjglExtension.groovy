package com.techshroom.inciseblue.lwjgl

import org.gradle.api.Project

class LwjglExtension {
    String compileConfiguration
    String lwjglVersion = null
    List<LwjglDep> dependencies = []

    String getCompileConfigurationFrom(Project project) {
        String confName = compileConfiguration
        if (confName == null) {
            // Use library API if not provided
            if (project.configurations.findByName('api') != null) {
                confName = 'api'
            } else if (project.configurations.findByName('implementation') != null) {
                confName = 'implementation'
            } else {
                confName = 'compile'
            }
        }
        return confName
    }

    String getCompileConfiguration() {
        return compileConfiguration
    }

    void setCompileConfiguration(String compileConfiguration) {
        this.compileConfiguration = compileConfiguration
    }

    void compileConfiguration(String compileConfiguration) {
        setCompileConfiguration(compileConfiguration)
    }

    String getLwjglVersion() {
        return lwjglVersion
    }

    void setLwjglVersion(String lwjglVersion) {
        this.lwjglVersion = lwjglVersion
    }

    void lwjglVersion(String lwjglVersion) {
        this.lwjglVersion = lwjglVersion
    }

    List<LwjglDep> getDependencies() {
        return dependencies
    }

    void setDependencies(List<LwjglDep> dependencies) {
        this.dependencies = dependencies
    }

    void addDependency(LwjglDep dep) {
        dependencies.add(dep)
    }

    void addDependency(String name, boolean natives = false) {
        addDependency(new LwjglDep(name, natives))
    }

    void validate() {
        if (lwjglVersion == null) {
            throw new IllegalArgumentException("LWJGL version must be set (inciseBlue.lwjgl.lwjglVersion)");
        }
    }
}
