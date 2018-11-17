import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.serialization.js.DynamicTypeDeserializer.id

plugins {
    id("com.gradle.plugin-publish") version "0.10.0"
    kotlin("jvm") version embeddedKotlinVersion
    `kotlin-dsl`
    `java-gradle-plugin`
    groovy
    id("net.researchgate.release") version "2.7.0"
    id("com.palantir.idea-test-fix") version "0.1.0"
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    compile(gradleApi())
    compile("gradle.plugin.net.minecrell:licenser:0.4.1")
    compileOnly(kotlin("stdlib-jdk8"))

    testCompile("junit:junit:4.12")
    testCompile("com.google.guava:guava:27.0-jre")
    testCompile("org.spockframework:spock-core:1.1-groovy-2.4") {
        exclude(group = "org.codehaus.groovy")
    }
}

pluginBundle {
    website = "https://techshroom.com"
    vcsUrl = "https://github.com/TechShroom/incise-blue"

    plugins {
        create("inciseBlue") {
            id = "${project.group}.${project.name}"
            displayName = "incise-blue plugin"
            tags = listOf(
                    "incise-blue", "maven-publish",
                    "java", "ide", "eclipse",
                    "intellij", "kotlin-dsl")
            description = "TechShroom's own build plugin. Extremely biased towards internal desires."
        }
    }
}

tasks.named("afterReleaseBuild").configure {
    dependsOn("publishPlugins")
}
