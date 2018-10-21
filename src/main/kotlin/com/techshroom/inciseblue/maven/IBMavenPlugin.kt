package com.techshroom.inciseblue.maven

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
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

class IBMavenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply(plugin = "java")
        val sourceJar = createSourceJarTask(project)
        val javadocJar = createJavadocJarTask(project)

        val pair = ossrhCreds(project)
        val (ossrhUsername, ossrhPassword) = pair
        if (ossrhUsername.isNullOrBlank() || ossrhPassword.isNullOrBlank()) {
            project.logger.lifecycle("[IBMaven] Skipping upload configuration due to missing username/password data.")
            return
        }

        val publishTaskProvider = project.tasks.named("publish")

        hookReleasePlugin(project, publishTaskProvider)

        val isSnapshot = (project.version as? String)?.endsWith("-SNAPSHOT") == true
        disableIfNeeded(publishTaskProvider, isSnapshot, project)

        configureMavenPublish(project, isSnapshot, ossrhUsername, ossrhPassword, sourceJar, javadocJar)
    }

    private fun configureMavenPublish(project: Project, isSnapshot: Boolean, ossrhUsername: String?, ossrhPassword: String?, sourceJar: NamedDomainObjectProvider<Jar>, javadocJar: NamedDomainObjectProvider<Jar>) {
        val cfg = project.ibExt.maven

        project.apply(plugin = "maven-publish")
        project.afterEvaluate {
            cfg.validate()

            project.configure<PublishingExtension> {
                addRepositories(project, isSnapshot, cfg, ossrhUsername, ossrhPassword)
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
            project.apply(plugin = "signing")
            project.configure<SigningExtension> {
                sign(project.extensions.getByType<PublishingExtension>()
                        .publications.getByName("maven"))
            }
        }
    }

    private fun MavenPublication.configureMavenPom(cfg: MavenExtension, project: Project, sourceJar: NamedDomainObjectProvider<Jar>, javadocJar: NamedDomainObjectProvider<Jar>) {
        artifactId = cfg.artifactName
        from(project.components.getByName("java"))
        artifact(sourceJar)
        artifact(javadocJar)

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

    private fun PublishingExtension.addRepositories(project: Project, isSnapshot: Boolean, cfg: MavenExtension, ossrhUsername: String?, ossrhPassword: String?) {
        repositories {
            maven {
                name = "IBMaven Publishing Repository"
                url = project.uri(when {
                    isSnapshot -> cfg.snapshotRepo
                    else -> cfg.repo
                })

                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
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

    private fun ossrhCreds(project: Project): Pair<String?, String?> {
        val ossrhUsername: String? = project.findProperty("ossrhUsername") as? String
        var ossrhPassword: String? = project.findProperty("ossrhPassword") as? String
        if (ossrhPassword == null) {
            ossrhPassword = System.getenv("OSSRH_PASSWORD") ?: null
        }
        return Pair(ossrhUsername, ossrhPassword)
    }

    private fun createSourceJarTask(project: Project): NamedDomainObjectProvider<Jar> {
        val sourceJar = project.tasks.register<Jar>("sourceJar")
        sourceJar.configure {
            dependsOn("classes")
            classifier = "sources"
            val sourceSets = project.extensions.getByType<SourceSetContainer>()
            from(sourceSets.named("main").get().allSource)
        }
        return sourceJar
    }

    private fun createJavadocJarTask(project: Project): NamedDomainObjectProvider<Jar> {
        val javadocJar = project.tasks.register<Jar>("javadocJar")
        javadocJar.configure {
            val jdTask = project.tasks.withType<Javadoc>().named("javadoc").get()
            dependsOn(jdTask)
            classifier = "javadoc"
            from(jdTask.destinationDir)
        }
        return javadocJar
    }
}
