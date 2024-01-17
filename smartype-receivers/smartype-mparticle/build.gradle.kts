plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version versions.kotlin
    id("com.android.library")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version versions.dokka
    kotlin("native.cocoapods")
}

repositories {
    google()
    mavenCentral()
}
val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

kotlin {
    androidTarget() {
        publishLibraryVariants("release")
    }
    js {
        browser()
    }
    ios {
        binaries.framework()
    }

    cocoapods {
        framework {
            summary = "MParticle Smartype"
            homepage = "."
            baseName = "mParticle_Smartype"
            ios.deploymentTarget = "14.3"
        }
        pod("mParticle-Apple-SDK/mParticle"){
            // Add these lines
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":smartype-api"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${versions.serialization}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val iosX64Main by getting {
            dependsOn(commonMain)
        }

        val iosArm64Main by getting {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                api(deps.mparticle.androidSdk)
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }
    }
}

tasks {
    val javadocJar by creating(Jar::class) {
        dependsOn(org.jetbrains.dokka.gradle.DokkaTask::class)
        archiveClassifier.set("javadoc")
        from(org.jetbrains.dokka.gradle.DokkaTask::class)
    }
}

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 19
        targetSdk = 33
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs(file("src/androidMain/kotlin"))
            res.srcDirs(file("src/androidMain/res"))
        }
    }
    lint {
        //TODO: remove this
        //due to a bug in mP Android SDK lint checks
        abortOnError = false
    }
}

publishing {
    repositories {
        maven {
            name = "staging"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
        maven {
            name = "snapshot"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

// Temporary workaround for https://youtrack.jetbrains.com/issue/KT-27170
configurations.create("compileClasspath")

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

apply(from = project.rootProject.file("gradle/publishing.gradle"))