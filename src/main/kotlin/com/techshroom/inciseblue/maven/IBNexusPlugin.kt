package com.techshroom.inciseblue.maven

import com.techshroom.inciseblue.ibExt
import de.marcphilipp.gradle.nexus.NexusPublishExtension
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

class IBNexusPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply(plugin = "java")
        val sourceJar = createSourceJarTask(project)
        val javadocJar = createJavadocJarTask(project)

        val isSnapshot = (project.version as? String)?.endsWith("-SNAPSHOT") == true
        configureNexusPublish(project, sourceJar, javadocJar)

        val publishTaskProvider = project.tasks.named("publish")

        hookReleasePlugin(project, publishTaskProvider)
        project.disableIfNeeded(publishTaskProvider, isSnapshot)
    }

    private fun configureNexusPublish(project: Project, sourceJar: NamedDomainObjectProvider<Jar>, javadocJar: NamedDomainObjectProvider<Jar>) {
        val cfg = project.ibExt.nexus

        project.apply(plugin = "de.marcphilipp.nexus-publish")
        project.configure<PublishingExtension> {
            publications {
                register<MavenPublication>("maven") {
                    configureMavenPom(cfg, project, sourceJar, javadocJar)
                }
            }
        }
        project.configure<NexusPublishExtension> {
            repositories {
                create("nexus") {
                    nexusUrl.set(cfg.repo)
                    snapshotRepositoryUrl.set(cfg.snapshotRepo)
                    username.set(cfg.username)
                    password.set(cfg.password)
                }
            }
        }
        project.afterEvaluate {
            cfg.signing.finalizeValue()
            if (cfg.signing.get()) {
                applySigning(project)
            }
        }
    }

    private fun MavenPublication.configureMavenPom(cfg: NexusExtension, project: Project, sourceJar: NamedDomainObjectProvider<Jar>, javadocJar: NamedDomainObjectProvider<Jar>) {
        artifactId = cfg.artifactName.get()
        from(project.components["java"])
        artifact(sourceJar.get())
        artifact(javadocJar.get())

        pom {
            val httpsUrl = cfg.coord.map { "https://github.com$it" }
            val gitUrl = cfg.coord.map { "git://github.com$it" }

            name.set(artifactId)
            description.set(cfg.projectDescription)
            url.set(httpsUrl)
            scm {
                connection.set(gitUrl)
                developerConnection.set(gitUrl)
                url.set(httpsUrl)
            }
            licenses {
                license {
                    name.set(cfg.licenseName)
                    url.set(cfg.licenseUrl.map { it.toString() })
                }
            }
            developers {
                project.afterEvaluate {
                    cfg.developers.finalizeValue()
                    cfg.developers.get().forEach { dev ->
                        developer {
                            id.set(dev.id)
                            name.set(dev.name)
                            email.set(dev.email)
                        }
                    }
                }
            }
        }
    }

}
