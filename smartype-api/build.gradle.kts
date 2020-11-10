import org.jetbrains.kotlin.cli.jvm.main
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version versions.kotlin
    id("com.android.library")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version versions.dokka
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

    explicitApi()

    js {
        browser()
    }
    jvm()
    android("android") {
        publishLibraryVariants("release")
    }
    ios() {
        binaries {
            framework("SmartypeApi", listOf(RELEASE)) {

            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${versions.serialization}")
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

        val androidMain by getting {
            dependsOn(commonMain)
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }

        val iosMain by getting {
            dependsOn(commonMain)
        }
    }
}
tasks {
//    val javadocJar by creating(Jar::class) {
//        dependsOn(dokka)
//        archiveClassifier.set("javadoc")
//        from(dokka)
//    }
}


android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(29)
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs(file("src/androidMain/kotlin"))
            res.srcDirs(file("src/androidMain/res"))
        }
    }
    lintOptions {
        //TODO: remove this
        //due to a bug in mP Android SDK lint checks
        isAbortOnError = false
    }
}

publishing {
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

signing {
    if (System.getenv("mavenSigningKeyId") != null) {
        sign(publishing.publications)
    }
}

apply(from = project.rootProject.file("gradle/publishing.gradle"))
