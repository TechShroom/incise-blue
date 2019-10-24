package com.techshroom.inciseblue.maven

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class NexusProjectTest extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def newBuildFile(String username = "nobody", String password = "important") {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.techshroom.incise-blue'
            }
            ext.ossrhUsername = "$username"
            ext.ossrhPassword = "$password"
            inciseBlue {
                nexus {
                    projectDescription = "Nothing special."
                    coords("nobody", "important")
                }
            }
        """
    }

    def "nexus plugin doesn't break build"() {
        when:
        newBuildFile()
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":tasks").outcome == TaskOutcome.SUCCESS
    }

    def "nexus plugin always applies publishing, even without credentials"() {
        when:
        newBuildFile("", "")
        buildFile << """
            task requirePublishApplied() {
                doLast {
                    assert project.findProperty("ossrhUsername") == ""
                    project.tasks.getByName("publishToMavenLocal")
                }
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('requirePublishApplied', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":requirePublishApplied").outcome == TaskOutcome.SUCCESS
    }

    def "nexus plugin doesn't apply signing if it's not possible"() {
        when:
        newBuildFile("", "")
        buildFile << """
            task requireNoSigning() {
                doLast {
                    assert project.tasks.findByName("signMavenPublication") == null
                }
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('requireNoSigning', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":requireNoSigning").outcome == TaskOutcome.SUCCESS
    }
}
