package com.techshroom.inciseblue.apt

import groovy.xml.MarkupBuilder
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class CreateFactoryPathFile extends AbstractTask {
    {
        mustRunAfter("cleanCreateFactoryPathFile")
        description = "Writes the factory path for Eclipse"
    }

    @InputFiles
    Collection<File> getAptFiles() {
        def resolvedConfiguration = project.configurations.getByName('annotationProcessor').resolvedConfiguration
        resolvedConfiguration.rethrowFailure()
        return resolvedConfiguration.files
    }

    @OutputFile
    File getFactoryFile() {
        return project.file(".factorypath")
    }

    @TaskAction
    void writeFactoryPathFile() {
        factoryFile.withWriter { out ->
            def markup = new MarkupBuilder(out)
            markup.factorypath {
                getAptFiles().each { aptFile ->
                    markup.factorypathentry(
                            kind: 'EXTJAR',
                            id: aptFile.absolutePath,
                            enabled: 'true',
                            runInBatchMode: 'false'
                    )
                }
            }
        }
    }
}
