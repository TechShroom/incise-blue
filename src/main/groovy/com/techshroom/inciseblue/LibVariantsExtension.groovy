package com.techshroom.inciseblue

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

import static com.techshroom.inciseblue.LibVariantsKt.commonLib

/**
 * Provides a better extension of LibVariants for Groovy.
 */
class LibVariantsExtension {

    static void commonLib(Project self,
                          String group,
                          String nameBase,
                          String version,
                          @DelegatesTo(LibVariantPicker) Closure<?> variantPicker) {
        commonLib(self, group, nameBase, version, ConfigureUtil.configureUsing(variantPicker))
    }
}
