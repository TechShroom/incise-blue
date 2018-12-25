package com.techshroom.inciseblue.util

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

fun Project.fixJavaCompilation(javaVersion: JavaVersion) {
    val javaHome = findJavaHome(javaVersion)

    plugins.withType<JavaBasePlugin> {
        tasks.withType<JavaCompile>().configureEach {
            // --release only works on >=9, although that's really the only place we need it.
            if (JavaVersion.toVersion(toolChain.version) >= JavaVersion.VERSION_1_9) {
                options.compilerArgs.addAll(listOf("--release", javaVersion.majorVersion))
            } else if (javaHome != null) {
                // attempt to use java home to set bootstrap compatibility
                options.bootstrapClasspath = project.fileTree("$javaHome/jre/lib")
            }
        }
    }

    // Use withId here, should allow us to avoid crashes when plugin isn't present.
    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType<KotlinJvmCompile> {
            kotlinOptions {
                jvmTarget = when {
                    javaVersion < JavaVersion.VERSION_1_6 -> throw IllegalArgumentException("Too early for Kotlin.")
                    javaVersion < JavaVersion.VERSION_1_8 -> "1.6"
                    else -> "1.8"
                }
                jdkHome = javaHome?.toAbsolutePath()?.toString()
            }
        }
    }
}

private fun Project.findJavaHome(javaVersion: JavaVersion): Path? {
    val major = javaVersion.majorVersion.toInt()
    (findProperty("java${major}Home") as? String)
            ?.let(project::file)
            ?.takeIf { it.exists() }
            ?.run(File::toPath)
            ?.also { return it }

    return when (JavaVersion.current()) {
        javaVersion -> Paths.get(System.getProperty("java.home"))
        else -> {
            val message = """
                Detected that you are building a project that compiles code in
                JDK $major, but the current JDK is ${JavaVersion.current().majorVersion}.
                Please set the property `java${major}Home` to a valid JDK $major home
                for correct build outcomes.
            """.trimIndent().replace('\n', ' ')
            message.chunked(80).forEach { logger.warn("[IBUtil] $it") }

            null
        }
    }
}