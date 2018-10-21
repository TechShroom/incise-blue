package com.techshroom.inciseblue.ide

import org.gradle.api.Action

open class IdeExtension {
    val eclipseConfiguration = EclipseConfiguration()
    var configureEclipse = true
    var configureIntellij = true

    fun configureEclipse(config: Action<EclipseConfiguration>? = null) {
        configureEclipse = true
        config?.execute(eclipseConfiguration)
    }

}