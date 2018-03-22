package com.techshroom.inciseblue.license

import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class IBLicensePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('com.github.hierynomus.license')

        LicenseExtension license = project.extensions.getByType(LicenseExtension)

        license.ext {
            name = project.name
            organization = project.rootProject.organization
            url = project.rootProject.url
        }
        license.with {
            header = project.rootProject.file('HEADER.txt')
            ignoreFailures = false
            strictCheck = true
            include '**/*.java'
            mapping java: 'SLASHSTAR_STYLE'
        }
    }
}
