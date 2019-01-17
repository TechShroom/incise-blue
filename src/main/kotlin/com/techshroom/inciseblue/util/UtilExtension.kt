package com.techshroom.inciseblue.util

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getPlugin
import javax.inject.Inject

open class UtilExtension @Inject constructor(private val project: Project) {
    var javaVersion: JavaVersion = JavaVersion.current()

    fun setJavaVersion(javaVersion: Any) {
        this.javaVersion = JavaVersion.toVersion(javaVersion)
    }

    var addRepositories: Boolean = true
    var setKotlinJvmTarget: Boolean = true
    var protectReleaseFromBadJdk: Boolean = true

    // a little bit of a hack -- but this is really a call we make, not a flag
    fun enableJUnit5() {
        project.plugins.getPlugin(IBUtilPlugin::class).apply {
            project.setupJUnit5()
        }
    }

    fun enableJavaBootstrapFixes() {
        project.afterEvaluate {
            fixJavaCompilation(javaVersion)
        }
    }
}
