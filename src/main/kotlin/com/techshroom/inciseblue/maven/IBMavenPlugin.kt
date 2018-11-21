package com.techshroom.inciseblue.maven

import com.techshroom.inciseblue.blankToNull
import com.techshroom.inciseblue.ibExt
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension

class IBMavenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply(plugin = "java")
        val sourceJar = createSourceJarTask(project)
        val javadocJar = createJavadocJarTask(project)

        val creds = ossrhCreds(project)

        val isSnapshot = (project.version as? String)?.endsWith("-SNAPSHOT") == true
        configureMavenPublish(project, isSnapshot, creds, sourceJar, javadocJar)

        val publishTaskProvider = project.tasks.named("publish")

        hookReleasePlugin(project, publishTaskProvider)
        disableIfNeeded(publishTaskProvider, isSnapshot, project)
    }

    private fun configureMavenPublish(project: Project, isSnapshot: Boolean, creds: Credentials?, sourceJar: NamedDomainObjectProvider<Jar>, javadocJar: NamedDomainObjectProvider<Jar>) {
        val cfg = project.ibExt.maven

        project.apply(plugin = "maven-publish")
        project.afterEvaluate {
            cfg.validate()

            project.configure<PublishingExtension> {
                if (creds != null) {
                    addRepositories(project, isSnapshot, cfg, creds)
                } else {
                    project.logger.lifecycle("[IBMaven] Disabling remote upload, no credentials found.")
                }
                publications {
                    create<MavenPublication>("maven") {
                        configureMavenPom(cfg, project, sourceJar, javadocJar)
                    }
                }
            }

            applySigningIfNeeded(cfg, project)
        }
    }

    private fun applySigningIfNeeded(cfg: MavenExtension, project: Project) {
        if (cfg.doSigning) {
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
    }

    private fun MavenPublication.configureMavenPom(cfg: MavenExtension, project: Project, sourceJar: NamedDomainObjectProvider<Jar>, javadocJar: NamedDomainObjectProvider<Jar>) {
        artifactId = cfg.artifactName
        from(project.components.getByName("java"))
        artifact(sourceJar.get())
        artifact(javadocJar.get())

        pom {
            val httpsUrl = "https://github.com${cfg.coord}"
            val gitUrl = "git://github.com${cfg.coord}"

            name.set(project.name)
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
                    url.set("$httpsUrl/blob/master${cfg.licensePath}")
                }
            }
            developers {
                cfg.developers.forEach { dev ->
                    this.developer {
                        id.set(dev.id)
                        name.set(dev.name)
                        email.set(dev.email)
                    }
                }
            }
        }
    }

    private fun PublishingExtension.addRepositories(project: Project, isSnapshot: Boolean, cfg: MavenExtension, creds: Credentials) {
        repositories {
            maven {
                name = "IBMaven Publishing Repository"
                url = project.uri(when {
                    isSnapshot -> cfg.snapshotRepo
                    else -> cfg.repo
                })

                credentials {
                    username = creds.username
                    password = creds.password
                }
            }
        }
    }

    private fun disableIfNeeded(publishTaskProvider: TaskProvider<Task>, isSnapshot: Boolean, project: Project) {
        publishTaskProvider.configure {
            onlyIf {
                val isTravis = System.getenv("TRAVIS")?.toBoolean() == true
                val enabled = isSnapshot || !isTravis
                if (!enabled) {
                    project.logger.lifecycle("[IBMaven] Disabling uploads for non-SNAPSHOT Travis build.")
                }
                enabled
            }
        }
    }

    private fun hookReleasePlugin(project: Project, publishTaskProvider: TaskProvider<Task>) {
        project.plugins.withId("net.researchgate.release") {
            project.tasks.named("afterReleaseBuild").configure {
                dependsOn(publishTaskProvider)
            }
        }
    }

    private data class Credentials(val username: String, val password: String)

    private fun ossrhCreds(project: Project): Credentials? {
        val usernameOptions = listOf(
                project.findProperty("ossrhUsername") as? String
        )
        val passwordOptions = listOf(
                project.findProperty("ossrhPassword") as? String,
                System.getenv("OSSRH_PASSWORD")
        )
        val username = usernameOptions
                .mapNotNull { it.blankToNull() }
                .firstOrNull()
        val password = passwordOptions
                .mapNotNull { it.blankToNull() }
                .firstOrNull()

        return when {
            username == null || password == null -> null
            else -> Credentials(username, password)
        }
    }

    private fun createSourceJarTask(project: Project): NamedDomainObjectProvider<Jar> {
        val sourceJar by project.tasks.registering(Jar::class) {
            dependsOn("classes")
            classifier = "sources"
            val sourceSets = project.extensions.getByType<SourceSetContainer>()
            from(sourceSets.named("main").get().allSource)
        }
        return sourceJar
    }

    private fun createJavadocJarTask(project: Project): NamedDomainObjectProvider<Jar> {
        val javadocJar by project.tasks.registering(Jar::class) {
            val jdTask = project.tasks.withType<Javadoc>().named("javadoc").get()
            dependsOn(jdTask)
            classifier = "javadoc"
            from(jdTask.destinationDir)
        }
        return javadocJar
    }
}
