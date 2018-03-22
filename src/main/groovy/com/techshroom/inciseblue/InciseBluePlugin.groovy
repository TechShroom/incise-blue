package com.techshroom.inciseblue

import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.annotation.Nonnull

class InciseBluePlugin implements Plugin<Project> {

    static InciseBlueExtension getExt(Project project) {
        return project.extensions.getByType(InciseBlueExtension)
    }

    @Override
    void apply(@Nonnull Project project) {
        project.extensions.create("inciseBlue", InciseBlueExtension.class, project)
    }

}
