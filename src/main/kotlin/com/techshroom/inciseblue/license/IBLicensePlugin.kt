package com.techshroom.inciseblue.license

import net.minecrell.gradle.licenser.LicenseExtension
import net.minecrell.gradle.licenser.Licenser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

class IBLicensePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply<Licenser>()

        project.extensions.getByType<LicenseExtension>().apply {
            val rp = project.rootProject
            // access "ext" field by casting to ExtAware
            (this as ExtensionAware).extensions.extraProperties.apply {
                set("name", rp.name)
                set("organization", rp.property("organization"))
                set("url", rp.property("url"))
            }

            header = rp.file("HEADER.txt")
            ignoreFailures = false
            include("**/*.java")
            include("**/*.kt")
            include("**/*.groovy")
        }
    }
}
