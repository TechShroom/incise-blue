package com.techshroom.inciseblue

import com.techshroom.inciseblue.lwjgl.LwjglExtension
import com.techshroom.inciseblue.maven.MavenExtension
import com.techshroom.inciseblue.util.UtilExtension
import org.gradle.api.Action
import org.gradle.api.Project

class InciseBlueExtension {

    private final Project project
    final LwjglExtension lwjgl
    final MavenExtension maven
    final UtilExtension util

    final InciseBluePluginApplication plugins

    InciseBlueExtension(Project project) {
        this.project = project
        this.lwjgl = project.objects.newInstance(LwjglExtension)
        this.maven = project.objects.newInstance(MavenExtension, project)
        this.util = project.objects.newInstance(UtilExtension)
        this.plugins = project.objects.newInstance(InciseBluePluginApplication, project)
    }

    void lwjgl(Action<LwjglExtension> config) {
        plugins.lwjgl()
        config.execute(lwjgl)
    }

    void maven(Action<MavenExtension> config) {
        plugins.maven()
        config.execute(maven)
    }

    void util(Action<UtilExtension> config) {
        plugins.util()
        config.execute(util)
    }

    void plugins(Action<InciseBluePluginApplication> config) {
        config.execute(plugins)
    }

}