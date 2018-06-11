package com.techshroom.inciseblue.util

import com.techshroom.inciseblue.InciseBluePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.plugins.ide.eclipse.model.EclipseModel

class IBUtilPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'java'
        project.apply plugin: 'eclipse'

        def util = InciseBluePlugin.getExt(project).util
        project.repositories { RepositoryHandler rh ->
            rh.jcenter()
            rh.maven { MavenArtifactRepository repo ->
                repo.name = "Sonatype Releases"
                repo.url = 'https://oss.sonatype.org/content/repositories/releases/'
                repo.metadataSources({ ms -> ms.mavenPom() })
            }
            rh.maven { MavenArtifactRepository repo ->
                repo.name = "Sonatype Snapshots"
                repo.url = 'https://oss.sonatype.org/content/repositories/snapshots/'
                repo.metadataSources({ ms -> ms.mavenPom() })
            }
        }
        project.afterEvaluate {
            def eclipse = project.extensions.getByType(EclipseModel)
            eclipse.classpath.containers.addAll(util.computeFullExtraContainers())
            eclipse.jdt.sourceCompatibility = util.javaVersion
            eclipse.jdt.targetCompatibility = util.javaVersion

            if (util.isJavaFx()) {
                eclipse.project {p ->
                    p.natures 'org.eclipse.xtext.ui.shared.xtextNature'
                    p.buildCommand 'org.eclipse.xtext.ui.shared.xtextBuilder'
                }
            }

            [JavaCompile, GroovyCompile, ScalaCompile].each { Class<? extends Task> ct ->
                project.tasks.withType(ct).each { Task t ->
                    t.sourceCompatibility = util.javaVersion.toString()
                    t.targetCompatibility = util.javaVersion.toString()
                }
            }
        }

        // apply some good configurations for the java tasks
        project.tasks.withType(Javadoc).each { Javadoc javadoc ->
            javadoc.options.addStringOption('Xdoclint:none', '-quiet')
        }

        project.tasks.withType(JavaCompile).each { JavaCompile compile ->
            compile.options.compilerArgs += ['-Xlint:all', '-Xlint:-processing', '-Xlint:-path']
            compile.options.deprecation = true
            compile.options.encoding = 'UTF-8'
            compile.options.incremental = true
            compile.options.fork = true
        }
    }
}
