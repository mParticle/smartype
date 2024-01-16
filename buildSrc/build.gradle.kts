import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
}
java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

kotlinDslPluginOptions {

}