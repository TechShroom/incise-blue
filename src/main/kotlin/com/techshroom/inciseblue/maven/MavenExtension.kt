package com.techshroom.inciseblue.maven

import org.gradle.api.Project
import javax.inject.Inject

open class MavenExtension @Inject constructor(project: Project) {
    var doSigning: Boolean = System.getenv("TRAVIS")?.toBoolean() != true
    var repo: String = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    var snapshotRepo: String = "https://oss.sonatype.org/content/repositories/snapshots/"
    lateinit var projectDescription: String
    lateinit var coord: String
    var artifactName: String = project.name
    var licenseName: String = "The MIT License"
    var licensePath: String = "/LICENSE"
    var developers: MutableList<Developer> = mutableListOf(
            Developer("kenzierocks", "Kenzie Togami", "ket1999@gmail.com")
    )

    fun coords(owner: String, repo: String) {
        coord = "/$owner/$repo"
    }

    fun addDeveloper(id: String, name: String, email: String) {
        addDeveloper(Developer(id, name, email))
    }

    fun addDeveloper(developer: Developer) {
        developers.add(developer)
    }

    fun validate() {
        if (!this::projectDescription.isInitialized) {
            throw IllegalArgumentException("Description must be set (inciseBlue.maven.description)")
        }
        if (!this::coord.isInitialized) {
            throw IllegalArgumentException("Coords must be set (inciseBlue.maven.coords)")
        }
        if (!coord.startsWith('/')) {
            coord = "/$coord"
        }
    }
}
