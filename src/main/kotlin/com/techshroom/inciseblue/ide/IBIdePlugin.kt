package com.techshroom.inciseblue.ide

import com.techshroom.inciseblue.ibExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.ide.eclipse.model.EclipseModel

class IBIdePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ideExt = project.ibExt.ide
        if (ideExt.configureEclipse) {
            project.configureEclipse(ideExt.eclipseConfiguration)
        }
        if (ideExt.configureIntellij) {
            project.configureIntellij()
        }
    }

    private fun Project.configureEclipse(conf: EclipseConfiguration) {
        apply(plugin = "eclipse")
        project.configure<EclipseModel> {
            classpath {
                containers.addAll(conf.allExtraContainers)
            }
            if (conf.addJavaFx) {
                project.natures("org.eclipse.xtext.ui.shared.xtextNature")
                project.buildCommand("org.eclipse.xtext.ui.shared.xtextBuilder")
            }
        }
    }

    private fun Project.configureIntellij() {
        apply(plugin = "idea")
    }
}

