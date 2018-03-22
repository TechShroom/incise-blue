package com.techshroom.inciseblue.apt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.eclipse.model.SourceFolder

class IBAptPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'java'
        project.apply plugin: 'eclipse'

        Configuration aptCompileOnly = project.configurations.create('aptCompileOnly')

        aptCompileOnly.description = 'Annotation Processors that are also needed on the compile classpath. Parent of annotationProcessor and compileOnly.'

        project.configurations.getByName('annotationProcessor').extendsFrom(aptCompileOnly)
        project.configurations.getByName('compileOnly').extendsFrom(aptCompileOnly)

        def createFactoryPathFile = project.tasks.create('createFactoryPathFile', CreateFactoryPathFile.class)

        def eclipseModel = project.extensions.getByType(EclipseModel.class)

        def generatedSourceFolder = '.apt_generated'
        eclipseModel.classpath.file.beforeMerged { Classpath cp ->
            def srcNode = new Node(null, "classpathentry")
            srcNode.attributes()['path'] = generatedSourceFolder
            def aptSrc = new SourceFolder(srcNode)
            aptSrc.entryAttributes['ignore_optional_problems'] = 'true'
            aptSrc.entryAttributes['optional'] = 'true'
            cp.entries.add(aptSrc)
        }

        eclipseModel.jdt.file.withProperties { props ->
            props.setProperty('org.eclipse.jdt.core.compiler.processAnnotations', 'enabled')
        }

        project.cleanEclipseClasspath.dependsOn('cleanCreateFactoryPathFile')
        project.eclipseClasspath.dependsOn(createFactoryPathFile)
    }
}
