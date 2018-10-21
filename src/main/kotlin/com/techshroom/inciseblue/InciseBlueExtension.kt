package com.techshroom.inciseblue

import com.techshroom.inciseblue.ide.IBIdePlugin
import com.techshroom.inciseblue.ide.IdeExtension
import com.techshroom.inciseblue.license.IBLicensePlugin
import com.techshroom.inciseblue.lwjgl.IBLwjglPlugin
import com.techshroom.inciseblue.lwjgl.LwjglExtension
import com.techshroom.inciseblue.maven.IBMavenPlugin
import com.techshroom.inciseblue.maven.MavenExtension
import com.techshroom.inciseblue.util.IBUtilPlugin
import com.techshroom.inciseblue.util.UtilExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.newInstance

open class InciseBlueExtension constructor(private val project: Project) {

    val lwjgl: LwjglExtension = project.objects.newInstance()
    val maven: MavenExtension = project.objects.newInstance(project)
    val util: UtilExtension = project.objects.newInstance()
    val ide: IdeExtension = project.objects.newInstance()

    private inline fun <reified T : Plugin<Project>> addPlugin() {
        project.pluginManager.apply(T::class.java)
    }

    fun license() {
        addPlugin<IBLicensePlugin>()
    }

    @JvmOverloads
    fun lwjgl(config: Action<LwjglExtension>? = null) {
        addPlugin<IBLwjglPlugin>()
        config?.execute(lwjgl)
    }

    @JvmOverloads
    fun maven(config: Action<MavenExtension>? = null) {
        addPlugin<IBMavenPlugin>()
        config?.execute(maven)
    }

    @JvmOverloads
    fun util(config: Action<UtilExtension>? = null) {
        addPlugin<IBUtilPlugin>()
        config?.execute(util)
    }

    @JvmOverloads
    fun ide(config: Action<IdeExtension>? = null) {
        addPlugin<IBIdePlugin>()
        config?.execute(ide)
    }

}