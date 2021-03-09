import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm") version versions.kotlin apply false
    id("com.diffplug.gradle.spotless") version versions.spotless
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/SmartypeObject.kt")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

buildscript {
    repositories {
        mavenCentral()
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
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
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