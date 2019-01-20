package com.techshroom.inciseblue

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

import static com.techshroom.inciseblue.LibVariantsKt.commonLib

/**
 * Provides a better extension of LibVariants for Groovy.
 */
class LibVariantsExtension {

    static LibVariantPicker commonLib(Project self,
                          String group,
                          String nameBase,
                          String version,
                          @DelegatesTo(LibVariantPicker) Closure<?> variantPicker = null) {
        def lib = commonLib(self, group, nameBase, version)
        if (variantPicker != null) {
            ConfigureUtil.configureUsing(variantPicker).execute(lib)
        }
        return lib
    }
}
