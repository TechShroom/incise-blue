package com.techshroom.inciseblue.util

import org.gradle.api.JavaVersion
import org.gradle.api.Project

import javax.inject.Inject

class UtilExtension {
    private JavaVersion javaVersion = null
    private List<String> extraContainers = []

    @Inject
    UtilExtension() {
    }

    /**
     * Java version to use for everything: IDE, JavaCompile, GroovyCompile, etc.
     */
    JavaVersion getJavaVersion() {
        return javaVersion
    }

    void setJavaVersion(Object javaVersion) {
        this.javaVersion = JavaVersion.toVersion(javaVersion)
    }

    List<String> getExtraContainers() {
        return extraContainers
    }

    void setExtraContainers(List<String> extraContainers) {
        this.extraContainers = extraContainers
    }

    void addJavaFx() {
        extraContainers.add('org.eclipse.fx.ide.jdt.core.JAVAFX_CONTAINER')
    }

    // helpers:
    /**
     * Get property from Gradle, Java system properties, or environment, in that order.
     */
    String getProperty(Project project, String propertyName) {
        if (project.hasProperty(propertyName))
            return project.property(propertyName)
        def res = System.getProperty(propertyName, null)
        if (res)
            return res
        res = System.getenv(propertyName)
        if (res)
            return res
        return null
    }

}
