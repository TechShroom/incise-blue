package com.techshroom.inciseblue.license

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LicenseProjectTest extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def newBuildFile() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """ 
            plugins {
                id 'com.techshroom.incise-blue'
            }
            inciseBlue {
                license()
                util()
            }
        """
        def gradleProps = testProjectDir.newFile("gradle.properties")
        gradleProps << """
            organization=foo
            url=https://example.com
        """
    }

    def "license plugin doesn't break build"() {
        when:
        newBuildFile()
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks', '-Si')
                .withPluginClasspath()
                .forwardOutput()
                .build()

        then:
        assert result.task(":tasks").outcome == TaskOutcome.SUCCESS
    }
}
