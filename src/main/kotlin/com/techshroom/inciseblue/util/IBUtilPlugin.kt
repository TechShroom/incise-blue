package com.techshroom.inciseblue.util

import com.techshroom.inciseblue.ibExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.scala.ScalaBasePlugin
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
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class IBUtilPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.addIBRepositories()
        project.setNiceJavadocOptions()
        project.setNiceJavaCompileOptions()
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

    private fun Project.setNiceJavadocOptions() {
        plugins.withType<JavaBasePlugin> {
            tasks.withType<Javadoc>().configureEach {
                (options as? StandardJavadocDocletOptions)
                        ?.addStringOption("Xdoclint:none", "-quiet")
            }
        }
    }

    private fun Project.setNiceJavaCompileOptions() {
        plugins.withType<JavaBasePlugin> {
            tasks.withType<JavaCompile>().configureEach {
                options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Xlint:-path"))
                options.isDeprecation = true
                options.encoding = "UTF-8"
                options.isIncremental = true
                options.isFork = true
            }
        }
    }

    // for extension
    internal fun Project.setupJUnit5() {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    private fun Project.hookPluginsForJavaVersion() {
        plugins.withType<JavaBasePlugin> {
            applyJavaVersion<JavaCompile>()
        }
        plugins.withType<GroovyBasePlugin> {
            applyJavaVersion<GroovyCompile>()
        }
        plugins.withType<ScalaBasePlugin> {
            applyJavaVersion<ScalaCompile>()
        }
        plugins.withType<EclipsePlugin> {
            applyJavaVersionToEclipse()
        }
        plugins.withType<IdeaPlugin> {
            applyJavaVersionToIntellij()
        }
        plugins.withId("org.jetbrains.kotlin.kapt") {
            applyJavaVersionToKapt()
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
                if (jdt != null) {
                    jdt {
                        val javaVersion = ibExt.util.javaVersion
                        sourceCompatibility = javaVersion
                        targetCompatibility = javaVersion
                    }
                }
            }
        }
    }

    private fun Project.applyJavaVersionToIntellij() {
        configure<IdeaModel> {
            val javaVersion = ibExt.util.javaVersion
            module.jdkName = javaVersion.toString()
            module.languageLevel = IdeaLanguageLevel(javaVersion)

            // set project if it's present too
            project?.jdkName = javaVersion.toString()
            project?.languageLevel = IdeaLanguageLevel(javaVersion)
        }
    }

    private fun Project.applyJavaVersionToKapt() {
        configure<KaptExtension> {
            javacOptions {
                option("-target", ibExt.util.javaVersion.toString())
                option("-source", ibExt.util.javaVersion.toString())
            }
        }
    }

}
