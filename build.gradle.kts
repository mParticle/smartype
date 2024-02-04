import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm") version versions.kotlin apply false
    id("com.diffplug.spotless") version versions.spotless
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
    }
    extra["signing.keyId"] = System.getenv("mavenSigningKeyId")
    extra["signing.secretKeyRingFile"] = System.getenv("mavenSigningKeyRingFile")
    extra["signing.password"] = System.getenv("mavenSigningKeyPassword")

    // workaround for a AGP/KMP bug
    // https://youtrack.jetbrains.com/issue/KT-46466
    // https://github.com/gradle/gradle/issues/26091
    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }
}