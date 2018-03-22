package com.techshroom.inciseblue

import com.techshroom.inciseblue.apt.IBAptPlugin
import com.techshroom.inciseblue.license.IBLicensePlugin
import com.techshroom.inciseblue.maven.IBMavenPlugin
import com.techshroom.inciseblue.util.IBUtilPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject
import java.lang.reflect.Method

class InciseBluePluginApplication {
    private final Project project

    @Inject
    InciseBluePluginApplication(Project project) {
        this.project = project
    }

    private void add(Class<? extends Plugin<?>> pluginClass) {
        project.pluginManager.apply pluginClass
    }

    def propertyMissing(String name) {
        try {
            Method m = getClass().getMethod(name)
            return m.invoke(this)
        } catch (NoSuchMethodException ignored) {
            // pass
        }
    }

    void apt() {
        add(IBAptPlugin.class)
    }

    void license() {
        add(IBLicensePlugin.class)
    }

    void maven() {
        add(IBMavenPlugin.class)
    }

    void util() {
        add(IBUtilPlugin.class)
    }

}