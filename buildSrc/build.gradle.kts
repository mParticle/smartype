import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
}
java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}