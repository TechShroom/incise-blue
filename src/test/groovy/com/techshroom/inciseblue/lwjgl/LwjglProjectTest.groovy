package com.techshroom.inciseblue.lwjgl

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LwjglProjectTest extends Specification {
    private static final String LWJGL_VERSION = "3.1.6"

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
                lwjgl {
                    lwjglVersion '${LWJGL_VERSION}'
                }
                util()
            }
        """
    }

    def "lwjgl plugin doesn't break build"() {
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

    def "lwjgl plugin adds dependencies"() {
        when:
        newBuildFile()
        buildFile << """
            inciseBlue.lwjgl {
                addDependency '', true
                addDependency 'opengl', true
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('dependencies', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":dependencies").outcome == TaskOutcome.SUCCESS
        assert result.output.contains("org.lwjgl:lwjgl:${LWJGL_VERSION}")
        assert result.output.contains("org.lwjgl:lwjgl-opengl:${LWJGL_VERSION}")
    }
}
