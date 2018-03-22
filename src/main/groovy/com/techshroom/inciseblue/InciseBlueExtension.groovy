package com.techshroom.inciseblue

import com.techshroom.inciseblue.maven.MavenExtension
import com.techshroom.inciseblue.util.UtilExtension
import org.gradle.api.Action
import org.gradle.api.Project

class InciseBlueExtension {

    private final Project project
    final MavenExtension maven
    final UtilExtension util

    final InciseBluePluginApplication plugins

    InciseBlueExtension(Project project) {
        this.project = project
        this.maven = project.objects.newInstance(MavenExtension, project)
        this.util = project.objects.newInstance(UtilExtension)
        this.plugins = project.objects.newInstance(InciseBluePluginApplication, project)
    }

    void maven(Action<MavenExtension> config) {
        config.execute(maven)
    }

    void util(Action<UtilExtension> config) {
        config.execute(util)
    }

    void plugins(Action<InciseBluePluginApplication> config) {
        config.execute(plugins)
    }

}