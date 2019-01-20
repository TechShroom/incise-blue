package com.techshroom.inciseblue

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

interface LibVariantPicker {
    fun lib(): Dependency
    fun lib(name: String): Dependency
}

operator fun LibVariantPicker.invoke(picker: Action<LibVariantPicker>) {
    picker.execute(this)
}

operator fun LibVariantPicker.invoke() = lib()
operator fun LibVariantPicker.invoke(name: String) = lib(name)

@JvmOverloads
fun Project.commonLib(group: String,
                      nameBase: String,
                      version: String,
                      picker: Action<LibVariantPicker>? = null): LibVariantPicker {
    return LibVariantPickerImpl(this, group, nameBase, version).also {
        picker?.execute(it)
    }
}

class LibVariantPickerImpl(
        private val project: Project,
        private val group: String,
        private val nameBase: String,
        private val version: String) : LibVariantPicker {
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
