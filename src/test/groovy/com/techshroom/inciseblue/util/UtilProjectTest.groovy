package com.techshroom.inciseblue.util

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class UtilProjectTest extends Specification {
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
                util()
            }
        """
    }

    def "util plugin doesn't break build"() {
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

    def "util plugin applies java versions"() {
        when:
        newBuildFile()
        buildFile << """
            inciseBlue.util.javaVersion = "1.8"
            apply plugin: "java"
            apply plugin: "idea"

            task javaCompileIs18() {
                doLast {
                    if (project.compileJava.sourceCompatibility != "1.8") {
                        throw new IllegalStateException("sourceCompatibility");
                    }
                    if (project.compileJava.targetCompatibility != "1.8") {
                        throw new IllegalStateException("targetCompatibility");
                    }
                    if (project.idea.project.jdkName != "1.8") {
                        throw new IllegalStateException("jdkName");
                    }
                    if (project.idea.project.languageLevel.level != "JDK_1_8") {
                        throw new IllegalStateException("jdkName");
                    }
                }
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('javaCompileIs18', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":javaCompileIs18").outcome == TaskOutcome.SUCCESS
    }

    def "util plugin skips eclipse java version if no plugin"() {
        // This used to crash, regression test
        when:
        newBuildFile()
        buildFile << """
            apply plugin: "eclipse"
            inciseBlue.util.javaVersion = "1.8"
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":tasks").outcome == TaskOutcome.SUCCESS
    }

    def "util plugin sets up JUnit Platform if requested"() {
        when:
        newBuildFile()
        buildFile << """
            apply plugin: "java"
            inciseBlue.util.enableJUnit5()

            task isUsingJunitPlatform() {
                doLast {
                    if (!(tasks.getByName("test").testFramework.class.name.contains("JUnitPlatform"))) {
                        throw new IllegalStateException()
                    }
                }
            }
        """
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('isUsingJunitPlatform', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":isUsingJunitPlatform").outcome == TaskOutcome.SUCCESS
    }
}
