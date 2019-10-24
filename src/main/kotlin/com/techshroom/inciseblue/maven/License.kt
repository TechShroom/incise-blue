package com.techshroom.inciseblue.maven

import java.net.URL

enum class License(
    val licenseName: String,
    val licenseUrl: URL
) {
    MIT(
        "MIT",
        URL("https://opensource.org/licenses/MIT")
    ),
    GPL3(
        "GPL-3.0",
        URL("https://opensource.org/licenses/GPL-3.0")
    )
}