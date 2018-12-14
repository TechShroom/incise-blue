package com.techshroom.inciseblue

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.util.ConfigureUtil

interface LibVariantPicker {
    fun lib(): Dependency
    fun lib(name: String): Dependency
}

fun Project.commonLib(group: String,
                      nameBase: String,
                      version: String,
                      variantPicker: Action<LibVariantPicker>) {
    val project = this
    val variantPickerDelegate: LibVariantPicker = object : LibVariantPicker {
        override fun lib() = libShared("")
        override fun lib(name: String) = libShared(name)
        private fun libShared(name: String): Dependency {
            val calcName = when {
                name.isEmpty() -> nameBase
                else -> "$nameBase-$name"
            }

            return project.dependencies.create(mapOf(
                    "group" to group,
                    "name" to calcName,
                    "version" to version))
        }
    }
    variantPicker.execute(variantPickerDelegate)
}
