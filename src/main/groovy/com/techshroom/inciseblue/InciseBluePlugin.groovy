package com.techshroom.inciseblue

import com.techshroom.inciseblue.apt.AptPlugin
import com.techshroom.inciseblue.license.LicensePlugin
import com.techshroom.inciseblue.maven.MavenExtension
import com.techshroom.inciseblue.maven.MavenPlugin
import com.techshroom.inciseblue.util.UtilExtension
import com.techshroom.inciseblue.util.UtilPlugin
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ObjectConfigurationAction

import javax.annotation.Nonnull
import javax.inject.Inject

class InciseBluePlugin implements Plugin<Project> {

    static InciseBlueExtension getExt(Project project) {
        return project.extensions.getByType(InciseBlueExtension)
    }

    @Override
    void apply(@Nonnull Project project) {
        project.extensions.create("inciseBlue", InciseBlueExtension.class, project)
    }

    static class InciseBlueExtension {

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

    static class InciseBluePluginApplication {
        private final Project project

        @Inject
        InciseBluePluginApplication(Project project) {
            this.project = project
        }

        private void add(String id) {
            project.apply { ObjectConfigurationAction apply ->
                apply.plugin(id)
            }
        }

        private static String id(String kind) {
            return "com.techshroom.incise-blue.${kind}"
        }

        void apt() {
            add(id('apt'))
        }

        void license() {
            add(id('license'))
        }

        void maven() {
            add(id('maven'))
        }

        void util() {
            add(id('util'))
        }

    }

}
