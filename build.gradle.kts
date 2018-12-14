import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
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

tasks.named<Copy>("processResources") {
    filter {
        it.replace("%VERSION%", project.version.toString())
    }
}

// include our Groovy extensions last
tasks {
    val compileKotlin = named<KotlinCompile>("compileKotlin")
    val compileGroovy = named<GroovyCompile>("compileGroovy")
    compileKotlin.configure {
        setDependsOn(dependsOn.minus("compileJava"))
    }
    compileGroovy.configure {
        dependsOn(compileKotlin)
        val kt = compileKotlin.get()
        classpath = classpath.plus(files(kt.destinationDir))
    }
    named<Task>("classes") {
        dependsOn(compileGroovy)
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

// validate the current JDK, we must be running under JDK 8.
val verifyJavaVersionForPublish by tasks.registering {
    description = "Validates the JDK for the publish build, to ensure consistency."
    doLast {
        if (JavaVersion.current() != JavaVersion.VERSION_1_8) {
            throw IllegalStateException("Must build release with JDK 8.")
        }
    }
}

tasks.named<Task>("publishPlugins") {
    dependsOn(verifyJavaVersionForPublish)
}

tasks.named<Task>("afterReleaseBuild") {
    dependsOn("publishPlugins")
}
