package com.techshroom.inciseblue

fun String?.blankToNull(): String? {
    return when {
        isNullOrBlank() -> null
        else -> this
    }
}
