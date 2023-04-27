
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version versions.kotlin
    id("com.android.library")
    kotlin("native.cocoapods")
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

val GROUP: String by project
val VERSION_NAME: String by project
val IS_PUBLISHED: String by project


group = GROUP
version = VERSION_NAME

kotlin {
    val xcFramework = XCFramework()
    js(IR) {
        browser()
        generateTypeScriptDefinitions()
        binaries.executable()
    }

    android() {
        publishLibraryVariants("release", "debug")
        mavenPublication {
            artifactId = "smartype"
        }
    }

    iosX64 {
        binaries.framework(listOf(NativeBuildType.RELEASE)) {
            xcFramework.add(this)
            embedBitcode("disable")
        }
    }

    iosArm64 {
        binaries.framework(listOf(NativeBuildType.RELEASE)) {
            xcFramework.add(this)
            embedBitcode("disable")
        }
    }

    cocoapods {
        framework {
            summary = "MParticle Smartype"
            homepage = "."
            baseName = "Smartype"
            ios.deploymentTarget = "14.3"
            if (IS_PUBLISHED.toBoolean()) {
                export("com.mparticle:smartype-mparticle:${project.version}")
                export("com.mparticle:smartype-api:${project.version}")
            } else {
                export(project(":smartype-api"))
                export(project(":smartype-receivers:smartype-mparticle"))
            }
        }
        pod("mParticle-Apple-SDK/mParticle")
    }

    tasks.create("iosFatFramework", org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask::class) {
        dependsOn(tasks["iosX64Binaries"])
        dependsOn(tasks["iosArm64Binaries"])
        baseName = "Smartype"

        destinationDir = buildDir.resolve("ios")

        // Specify the frameworks to be merged.
        val iosX64 = targets.getByName("iosX64") as KotlinNativeTarget
        val iosArm64 = targets.getByName("iosArm64") as KotlinNativeTarget
        from(
            kotlin.iosArm64().binaries.getFramework("RELEASE"),
            kotlin.iosX64().binaries.getFramework("RELEASE")
        )
    }

    tasks.register<Copy>("releaseFatFramework") {
        dependsOn(tasks["iosFatFramework"])
        from("$buildDir/Smartype/release")
        val binDirectory: String? by project
        if (binDirectory != null) {
            into(binDirectory)
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("${project(":smartype-generator").buildDir}/generatedWebSources")
            kotlin.srcDir("${project(":smartype-generator").buildDir}/generatedSources")
            dependencies {
                if (IS_PUBLISHED.toBoolean()) {
                    api("com.mparticle:smartype-mparticle:${project.version}")
                    api("com.mparticle:smartype-api:${project.version}")
                } else {
                    api(project(":smartype-api"))
                    api(project(":smartype-receivers:smartype-mparticle"))
                }
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${versions.serialization}")
            }
        }
        val commonTest by getting
        if (commonTest != null) {
            commonTest {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test"))
                    implementation(kotlin("test-junit"))
                    implementation(kotlin("test-annotations-common"))
                }
            }
        }

        val androidMain by getting
        if (androidMain != null) {
            androidMain.dependsOn(commonMain)
        }

        try {
            val jsMain by getting
            if (jsMain != null) {
                jsMain.dependsOn(commonMain)
            }
        } catch (e: kotlin.Exception) {
        }

        try {
            val iosX64Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/iosMain")
            }
        } catch (e: kotlin.Exception) {
        }

        try {
            val iosArm64Main by getting {
                dependsOn(commonMain)
                kotlin.srcDir("src/iosMain")
            }
        } catch (e: kotlin.Exception) {
        }
    }

}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 19
        targetSdk = 31
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs(file("src/androidMain/kotlin"))
            res.srcDirs(file("src/androidMain/res"))
        }
    }
}

