plugins {
    kotlin("jvm")
    id("application")
    id("maven-publish")
    id("signing")
    kotlin("plugin.serialization") version versions.kotlin
}

application {
    mainClassName = "com.mparticle.smartype.generator.GeneratorKt"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("com.github.ajalt:clikt:2.6.0")
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${versions.serialization}")
    api(project(path=":smartype-api", configuration = "jvmDefault"))
}
java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}
//create a single Jar with all dependencies
val fatJar = task("fatJar", type = Jar::class) {
    with(tasks.jar.get() as CopySpec)
    from("..")
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName ))
    }
    includeEmptyDirs = false
    exclude(
        "**/.gradle",
        "**/examples",
        "**/.idea",
        "**/build",
        "**/test",
        "**/*.md",
        "**/*.iml",
        "**/[.].*",
        "**/[.]",
        "**/Carthage",
        "**/smartype-receivers",
        "**/smartype-api",
        "**/local.properties")

    rename(
        "settings.gradle.package.kts",
        "settings.gradle.kts"
    )
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

publishing {
    publications {
        create<MavenPublication>("sonatype") {
            artifactId = "smartype-generator"
            artifact(fatJar)
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            pom {
                name.set("Smartype Generator")
                description.set("Generator ")
                url.set("https://github.com/mParticle/smartype")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("samdozor")
                        name.set("Sam Dozor")
                        email.set("sdozor@mparticle.com")
                    }
                    developer {
                        id.set("peterjenkins")
                        name.set("Peter Jenkins")
                        email.set("pjenkins@mparticle.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/mParticle/smartype.git")
                    developerConnection.set("scm:git:ssh://github.com/mParticle/smartype.git")
                    url.set("https://github.com/mParticle/smartype")
                }
            }
        }
    }
    repositories {
        maven {
            name = "staging"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("sonatypeUsername")
                password = System.getenv("sonatypePassword")
            }
        }
        maven {
            name = "snapshot"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = System.getenv("sonatypeUsername")
                password = System.getenv("sonatypePassword")
            }
        }
    }
}

allprojects {
    extra["signing.keyId"] = System.getenv("mavenSigningKeyId")
    extra["signing.secretKeyRingFile"] = System.getenv("mavenSigningKeyRingFile")
    extra["signing.password"] = System.getenv("mavenSigningKeyPassword")
}

signing {
    if (System.getenv("mavenSigningKeyId") != null) {
        sign(publishing.publications["sonatype"])
    }
}
