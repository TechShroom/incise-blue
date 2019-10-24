package com.techshroom.inciseblue.maven

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension


internal fun createSourceJarTask(project: Project): NamedDomainObjectProvider<Jar> {
    return project.tasks.register<Jar>("sourceJar") {
        dependsOn("classes")
        archiveClassifier.set("sources")
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
        from(sourceSets.named("main").get().allSource)
    }
}

internal fun createJavadocJarTask(project: Project): NamedDomainObjectProvider<Jar> {
    return project.tasks.register<Jar>("javadocJar") {
        val jdTask = project.tasks.withType<Javadoc>().named("javadoc").get()
        dependsOn(jdTask)
        archiveClassifier.set("javadoc")
        from(jdTask.destinationDir)
    }
}

internal fun applySigning(project: Project) {
    project.logger.lifecycle("[IBMaven] Signing enabled.")
    project.apply(plugin = "signing")
    project.configure<SigningExtension> {
        // Only sign if it's possible.
        if (this.signatories.getDefaultSignatory(project) != null) {
            sign(project.extensions.getByType<PublishingExtension>()
                .publications.getByName("maven"))
        }
    }
}

internal fun Project.disableIfNeeded(publishTaskProvider: TaskProvider<Task>, isSnapshot: Boolean) {
    publishTaskProvider.configure {
        onlyIf {
            val isTravis = System.getenv("TRAVIS")?.toBoolean() == true
            val enabled = isSnapshot || !isTravis
            if (!enabled) {
                this@disableIfNeeded.logger.lifecycle(
                    "[IBMaven] Disabling uploads for non-SNAPSHOT Travis build."
                )
            }
            enabled
        }
    }
}

internal fun hookReleasePlugin(project: Project, publishTaskProvider: TaskProvider<Task>) {
    project.plugins.withId("net.researchgate.release") {
        project.tasks.named("afterReleaseBuild").configure {
            dependsOn(publishTaskProvider)
        }
    }
}
