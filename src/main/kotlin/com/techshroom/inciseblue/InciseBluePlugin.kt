package com.techshroom.inciseblue

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

import javax.annotation.Nonnull

class InciseBluePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create<InciseBlueExtension>("inciseBlue", project)
    }

}
