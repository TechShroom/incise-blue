package com.techshroom.inciseblue.maven

import com.techshroom.inciseblue.blankToNull
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.net.URI
import java.net.URL
import javax.inject.Inject

abstract class NexusExtension @Inject constructor(project: Project) {
    abstract val signing: Property<Boolean>
    abstract val repo: Property<URI>
    abstract val snapshotRepo: Property<URI>
    abstract val projectDescription: Property<String>
    abstract val coord: Property<String>
    abstract val artifactName: Property<String>
    abstract val licenseName: Property<String>
    abstract val licenseUrl: Property<URL>
    abstract val developers: ListProperty<Developer>
    abstract val username: Property<String>
    abstract val password: Property<String>

    init {
        signing.convention(System.getenv("CI")?.toBoolean() != true)
        repo.convention(project.uri(
            "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        ))
        snapshotRepo.convention(project.uri(
            "https://oss.sonatype.org/content/repositories/snapshots/"
        ))
        artifactName.convention(project.name)
        username.convention(project.provider {
            val usernameOptions = listOf(
                project.findProperty("ossrhUsername") as? String
            )
            usernameOptions
                .mapNotNull { it.blankToNull() }
                .firstOrNull()
        })
        password.convention(project.provider {
            val passwordOptions = listOf(
                project.findProperty("ossrhPassword") as? String,
                System.getenv("OSSRH_PASSWORD")
            )
            passwordOptions
                .mapNotNull { it.blankToNull() }
                .firstOrNull()
        })
    }

    fun addDeveloper(id: String, name: String, email: String) {
        addDeveloper(Developer(id, name, email))
    }

    fun addDeveloper(developer: Developer) {
        developers.add(developer)
    }

    fun coords(owner: String, repo: String) {
        coord.set("/$owner/$repo")
    }
}