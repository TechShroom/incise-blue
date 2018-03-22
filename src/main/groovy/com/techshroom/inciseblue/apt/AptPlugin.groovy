package com.techshroom.inciseblue.apt

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.plugins.ide.eclipse.model.EclipseModel

class AptPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'java'
        project.apply plugin: 'eclipse'

        Configuration aptCompileOnly = project.configurations.create('aptCompileOnly')

        aptCompileOnly.description = 'Annotation Processors that are also needed on the compile classpath. Parent of annotationProcessor and compileOnly.'

        project.configurations.getByName('annotationProcessor').extendsFrom(aptCompileOnly)
        project.configurations.getByName('compileOnly').extendsFrom(aptCompileOnly)

        def createFactoryPathFile = project.tasks.create('createFactoryPathFile', CreateFactoryPathFile.class)

        def eclipseModel = project.extensions.getByType(EclipseModel.class)

        eclipseModel.classpath.file.withXml {
            def node = it.asNode()
            def attrNode = node.appendNode('classpathentry', ['kind': 'src', 'path': '.apt_generated'])
                    .appendNode('attributes');
            attrNode.appendNode('attribute', ['name': 'ignore_optional_problems', 'value': 'true']);
            attrNode.appendNode('attribute', ['name': 'optional', 'value': 'true']);
        }

        eclipseModel.jdt.file.withProperties { props ->
            props.setProperty('org.eclipse.jdt.core.compiler.processAnnotations', 'enabled')
        }

        project.cleanEclipseClasspath.dependsOn('cleanCreateFactoryPathFile')
        project.eclipseClasspath.dependsOn(createFactoryPathFile)
    }
}
