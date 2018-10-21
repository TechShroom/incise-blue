package com.techshroom.inciseblue

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

val Project.ibExt
    get() = this.extensions.getByType<InciseBlueExtension>()