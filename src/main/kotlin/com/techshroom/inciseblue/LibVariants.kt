package com.techshroom.inciseblue

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

interface LibVariantPicker {
    fun lib(name: String? = null): Dependency
}

fun Project.commonLib(group: String,
                      nameBase: String,
                      version: String,
                      variantPicker: LibVariantPicker.() -> Unit) {
    val project = this
    val variantPickerDelegate: LibVariantPicker = object : LibVariantPicker {
        override fun lib(name: String?): Dependency {
            val calcName = when {
                name.isNullOrEmpty() -> nameBase
                else -> "$nameBase-$name"
            }

            return project.dependencies.create(mapOf(
                    "group" to group,
                    "name" to calcName,
                    "version" to version))
        }
    }
    variantPickerDelegate.apply(variantPicker)
}
