package com.techshroom.inciseblue.jfx

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class JfxProjectTest extends Specification {
    private static final String JFX_VERSION = "3.1.6"

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def newBuildFile() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.techshroom.incise-blue'
                id 'java'
            }
            inciseBlue {
                jfx {
                    jfxVersion '${JFX_VERSION}'
                }
                util()
            }
        """
    }

    def "jfx plugin doesn't break build"() {
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

    def "jfx plugin adds dependencies"() {
        when:
        newBuildFile()
        buildFile << """
            inciseBlue.jfx {
                addDependency 'controls'
                addDependency 'fxml'
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dependencies', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":dependencies").outcome == TaskOutcome.SUCCESS
        assert result.output.contains("org.openjfx:javafx-controls:${JFX_VERSION}")
        assert result.output.contains("org.openjfx:javafx-fxml:${JFX_VERSION}")
    }
}
