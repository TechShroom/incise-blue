package com.techshroom.inciseblue

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal val Project.ibExt
    get() = this.extensions.getByType<InciseBlueExtension>()

internal fun String.formatForLogging(prefix: String, width: Int = 80): List<String> {
    val currentLine = StringBuilder(prefix)
    val outputLines = mutableListOf<String>()
    for (word in splitToSequence(' ')) {
        // if [prefix + line] + [space] + [word] > width, new line
        if (currentLine.length + 1 + word.length > width) {
            outputLines.add(currentLine.toString())
            currentLine.setLength(prefix.length)
        }
        currentLine.append(' ').append(word)
    }
    outputLines.add(currentLine.toString())
    return outputLines
}