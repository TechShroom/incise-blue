package com.techshroom.inciseblue.lwjgl

import com.techshroom.inciseblue.InciseBluePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class IBLwjglPlugin implements Plugin<Project> {

    private static List<Object> lwjglJarDep(LwjglExtension ext, String name) {
        def lwjgl = getLwjglName(name)
        def ver = ext.lwjglVersion
        return [
                [group: 'org.lwjgl', name: lwjgl, version: ver]
        ]
    }

    private static List<Object> lwjglNativeDep(LwjglExtension ext, String name) {
        def lwjgl = getLwjglName(name)
        def ver = ext.lwjglVersion
        return [
                [group: 'org.lwjgl', name: lwjgl, version: ver, classifier: 'natives-macos'],
                [group: 'org.lwjgl', name: lwjgl, version: ver, classifier: 'natives-linux'],
                [group: 'org.lwjgl', name: lwjgl, version: ver, classifier: 'natives-windows']
        ]
    }

    private static String getLwjglName(String name) {
        "lwjgl" + (name ? "-" + name : "")
    }

    void apply(Project project) {
        def cfg = InciseBluePlugin.getExt(project).lwjgl

        project.afterEvaluate {
            cfg.validate()
            def compile = cfg.getCompileConfigurationFrom(project)
            cfg.dependencies.each {
                lwjglJarDep(cfg, it.name).each { dep -> project.dependencies.add(compile, dep) }
                if (it.natives) {
                    lwjglNativeDep(cfg, it.name).each { dep -> project.dependencies.add(compile, dep) }
                }
            }
        }
    }
}
