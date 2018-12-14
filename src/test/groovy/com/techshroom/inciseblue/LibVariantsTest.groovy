package com.techshroom.inciseblue

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LibVariantsTest extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def newBuildFile() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            import com.techshroom.inciseblue.LibVariantsExtension
            plugins {
                id 'com.techshroom.incise-blue'
            }
        """
    }

    def "can use commonLib in groovy"() {
        when:
        newBuildFile()
        buildFile << """
            dependencies {
                LibVariantsExtension.commonLib(project, "group", "name", "version") {
                    def libA = lib()
                    def libB = lib("b")
                    assert [libA, libB].every { it.group == "group" }
                    assert [libA, libB].every { it.version == "version" }
                    assert libA.name == "name"
                    assert libB.name == "name-b"
                }
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks', '-Si')
                .withPluginClasspath()
                .build()

        then:
        result.task(":tasks").outcome == TaskOutcome.SUCCESS
    }
}
