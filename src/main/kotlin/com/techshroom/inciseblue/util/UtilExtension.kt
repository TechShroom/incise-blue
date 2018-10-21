package com.techshroom.inciseblue.util

import org.gradle.api.JavaVersion

open class UtilExtension {
    var junit5: Boolean = false
    var javaVersion: JavaVersion = JavaVersion.current()

    fun setJavaVersion(javaVersion: Any) {
        this.javaVersion = JavaVersion.toVersion(javaVersion)
    }

}
