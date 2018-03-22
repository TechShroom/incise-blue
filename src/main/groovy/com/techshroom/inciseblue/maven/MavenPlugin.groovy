package com.techshroom.inciseblue.maven

import com.techshroom.inciseblue.InciseBluePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class MavenPlugin implements Plugin<Project> {
    void apply(Project project) {
        // Add source/jd tasks
        project.apply plugin: 'java'
        project.task('sourcesJar', type: Jar, dependsOn: project.classes) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }

        project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
            classifier = 'javadoc'
            from project.javadoc.destinationDir
        }

        project.artifacts {
            archives project.sourcesJar
            archives project.javadocJar
        }

        def cfg = InciseBluePlugin.getExt(project).maven
        if (!project.hasProperty('ossrhPassword')) {
            project.ext.ossrhPassword = System.getenv('PASSWORD')
        }
        // skip everything if data missing
        if (!(project.hasProperty('ossrhUsername') && project.hasProperty('ossrhPassword') && project.property('ossrhPassword'))) {
            project.logger.lifecycle("[IBMaven] Skipping upload configuration due to missing username/password data.")
            return
        }

        project.plugins.withId('net.researchgate.release') {
            project.afterReleaseBuild.dependsOn(project.uploadArchives)
        }
        if (!project.version.endsWith('-SNAPSHOT') && Boolean.parseBoolean(System.getenv('TRAVIS'))) {
            project.logger.lifecycle('[IBMaven] Disabling uploads for non-SNAPSHOT Travis build.')
            project.uploadArchives.onlyIf { false }
        }
        project.logger.info("[IBMaven] username:password=${project.ossrhUsername}:REDACTED")
        project.apply plugin: 'maven'
        project.afterEvaluate {
            cfg.validate()
            def installer = project.install.repositories.mavenInstaller;
            project.uploadArchives.repositories {
                mavenDeployer {}
            }
            def deployer = project.uploadArchives.repositories.mavenDeployer;
            if (cfg.snapshotRepo) {
                project.project.configure([deployer]) {
                    snapshotRepository(url: cfg.snapshotRepo) {
                        authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
                    }
                }
            }
            project.configure([deployer]) {
                repository(url: cfg.repo) {
                    authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
                }
            }
            project.configure([installer, deployer]) {
                pom.project {
                    name project.name
                    packaging 'jar'
                    description cfg.projectDescription
                    url 'https://github.com' + cfg.coord

                    scm {
                        connection 'git://github.com' + cfg.coord
                        developerConnection 'git://github.com' + cfg.coord
                        url 'https://github.com' + cfg.coord
                    }

                    licenses {
                        license {
                            name 'The MIT License'
                            url "https://github.com${cfg.coord}/blob/master/LICENSE"
                        }
                    }

                    developers {
                        developer {
                            id 'kenzierocks'
                            name 'Kenzie Togami'
                            email 'ket1999@gmail.com'
                        }
                    }
                }
                pom.artifactId = cfg.artifactName
            }
            if (cfg.doSigning) {
                project.apply plugin: 'signing'
                project.signing {
                    sign project.configurations.archives
                }
                project.configure([deployer]) {
                    beforeDeployment { deployment -> project.signing.signPom(deployment) }
                }
            }
        }
    }
}
