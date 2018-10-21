package com.techshroom.inciseblue.util

import com.techshroom.inciseblue.ibExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel

class IBUtilPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.addIBRepositories()
        project.fixJavadocTasks()
        project.fixJavaCompileTasks()
        if (project.ibExt.util.junit5) {
            project.setupJunit5()
        }
        project.hookPluginsForJavaVersion()
    }

    private fun Project.addIBRepositories() {
        repositories {
            jcenter()
            maven {
                name = "Sonatype Releases"
                url = uri("https://oss.sonatype.org/content/repositories/releases/")
                metadataSources {
                    mavenPom()
                }
            }
            maven {
                name = "Sonatype Snapshots"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                metadataSources {
                    mavenPom()
                }
            }
        }
    }

    private fun Project.fixJavadocTasks() {
        plugins.withId("java") {
            tasks.withType<Javadoc>().configureEach {
                (options as? StandardJavadocDocletOptions)
                        ?.addStringOption("Xdoclint:none", "-quiet")
            }
        }
    }

    private fun Project.fixJavaCompileTasks() {
        plugins.withId("java") {
            tasks.withType<JavaCompile>().configureEach {
                options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Xlint:-path"))
                options.isDeprecation = true
                options.encoding = "UTF-8"
                options.isIncremental = true
                options.isFork = true
            }
        }
    }

    private fun Project.setupJunit5() {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    private fun Project.hookPluginsForJavaVersion() {
        plugins.withId("java") {
            applyJavaVersion<JavaCompile>()
        }
        plugins.withId("groovy") {
            applyJavaVersion<GroovyCompile>()
        }
        plugins.withId("scala") {
            applyJavaVersion<ScalaCompile>()
        }
        plugins.withId("eclipse") {
            applyJavaVersionToEclipse()
        }
        plugins.withId("intellij") {
            applyJavaVersionToIntellij()
        }
    }

    private inline fun <reified COMPILE_TASK : AbstractCompile> Project.applyJavaVersion() {
        tasks.withType<COMPILE_TASK>().configureEach {
            val javaVersion = ibExt.util.javaVersion.toString()
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }

    private fun Project.applyJavaVersionToEclipse() {
        configure<EclipseModel> {
            classpath {
                jdt {
                    val javaVersion = ibExt.util.javaVersion
                    sourceCompatibility = javaVersion
                    targetCompatibility = javaVersion
                }
            }
        }
    }

    private fun Project.applyJavaVersionToIntellij() {
        configure<IdeaModel> {
            val javaVersion = ibExt.util.javaVersion
            module.jdkName = javaVersion.toString()
            module.languageLevel = IdeaLanguageLevel(javaVersion)
        }
    }

}
