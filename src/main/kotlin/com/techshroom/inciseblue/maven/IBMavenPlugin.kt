package com.techshroom.inciseblue.maven

import com.techshroom.inciseblue.blankToNull
import com.techshroom.inciseblue.ibExt
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

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
        project.disableIfNeeded(publishTaskProvider, isSnapshot)
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

            if (cfg.doSigning) {
                applySigning(project)
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

}
