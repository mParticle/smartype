import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.out

plugins {
    kotlin("jvm") version versions.kotlin apply false
    id("com.diffplug.gradle.spotless") version versions.spotless
}

spotless {
    kotlin {
        target("**/*.kt")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(deps.android.gradlePlugin)
        classpath(kotlin("gradle-plugin", version = versions.kotlin))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven ("https://dl.bintray.com/kotlin/kotlin-eap")
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    extra["signing.keyId"] = System.getenv("mavenSigningKeyId")
    extra["signing.secretKeyRingFile"] = System.getenv("mavenSigningKeyRingFile")
    extra["signing.password"] = System.getenv("mavenSigningKeyPassword")
}