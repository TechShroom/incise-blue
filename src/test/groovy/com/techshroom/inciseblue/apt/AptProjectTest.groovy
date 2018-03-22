package com.techshroom.inciseblue.apt

import com.google.common.io.Resources
import groovy.util.slurpersupport.GPathResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AptProjectTest extends Specification {
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def newBuildFile(String depBlock) {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """ 
            plugins {
                id 'com.techshroom.incise-blue'
            }
            inciseBlue.plugins {
                apt()
            }
            repositories {
                jcenter()
            }
            dependencies {
                ${depBlock}
            }
        """
    }

    def "apt plugin writes factory file"() {
        when:
        newBuildFile("annotationProcessor group: 'com.google.auto.value', name: 'auto-value', version: '1.5.4'")
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('createFactoryPathFile', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":createFactoryPathFile").outcome == TaskOutcome.SUCCESS

        def fpFile = new File(testProjectDir.getRoot(), ".factorypath")

        assert fpFile.exists()

        def xml = new XmlSlurper().parse(fpFile)
        GPathResult entries = xml.factorypathentry

        assert entries.any { e ->
            def file = new File(e['@id'].toString())
            file.exists() && file.toString().contains('com.google.auto.value')
        }
    }

    // Compiles a class that requires the annotation in the dependency
    def "apt plugin adds aptCompileOnly"() {
        when:
        newBuildFile("aptCompileOnly group: 'com.google.auto.value', name: 'auto-value', version: '1.5.4'")

        def srcRoot = new File(testProjectDir.root, "src/main/java/")
        assert srcRoot.mkdirs()
        new File(srcRoot, "AptCompileOnly.java").withOutputStream { out ->
            Resources.copy(
                    Resources.getResource("com/techshroom/inciseblue/apt/AptCompileOnly.java"),
                    out
            )
        }

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('compileJava', '-Si')
                .withPluginClasspath()
                .build()

        then:
        assert result.task(":compileJava").outcome == TaskOutcome.SUCCESS
    }
}
