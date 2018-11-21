package com.techshroom.inciseblue.maven

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class MavenProjectTest extends Specification {
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
                maven {
                    projectDescription = "Nothing special."
                    coords("nobody", "important")
                }
            }
        """
    }

    def "maven plugin doesn't break build"() {
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

    def "maven plugin always applies publishing, even without credentials"() {
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
}
