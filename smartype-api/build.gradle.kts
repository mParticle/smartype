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

    js("js") {
        browser()
    }
    jvm("jvm") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    androidTarget("android") {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    iosX64() {
        binaries {
            framework("SmartypeApi", listOf(RELEASE)) {

            }
        }
    }
    iosArm64() {
        binaries {
            framework("SmartypeApi", listOf(RELEASE)) {

            }
        }
    }
    iosSimulatorArm64() {
        binaries {
            framework("SmartypeApi", listOf(RELEASE)) {

            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${versions.serialization}")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-annotations-common"))
            }
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
    namespace = "com.mparticle.smartype.api"
    compileSdk = 33
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
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

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

apply(from = project.rootProject.file("gradle/publishing.gradle"))
