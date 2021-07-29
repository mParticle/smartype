import org.jetbrains.kotlin.cli.jvm.main
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version versions.kotlin
    id("com.android.library")
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

val carthageBuildDir = "$projectDir/Carthage/Build/iOS"

kotlin {

        js {
            browser {
                webpackTask {
                    output.libraryTarget = "commonjs2"
                }
            }
            binaries.executable()
        }

    android() {
        publishLibraryVariants("release", "debug")
        mavenPublication {
            artifactId = "smartype"
        }
    }

    ios() {
        compilations {
            getByName("main") {
                source(sourceSets.getByName("iosMain"))
                kotlinOptions.freeCompilerArgs = listOf("-verbose")
            }
        }
        binaries {
            framework(listOf(RELEASE)) {
                baseName = "Smartype"
                transitiveExport = true
                if (IS_PUBLISHED.toBoolean()) {
                    export("com.mparticle:smartype-mparticle:${project.version}")
                    export("com.mparticle:smartype-api:${project.version}")
                } else {
                    export(project(":smartype-api"))
                    export(project(":smartype-receivers:smartype-mparticle"))
                }
                linkerOpts.add("-F${carthageBuildDir}")
                linkerOpts.add("-framework")
                linkerOpts.add("mParticle_Apple_SDK")
            }
        }
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
        }catch (e: kotlin.Exception){}

        try {
            val iosX64Main by getting {
                dependsOn(commonMain)
            }
        }catch (e: kotlin.Exception){}

        try {
            val iosArm64Main by getting {
                dependsOn(commonMain)
            }
        }catch (e: kotlin.Exception){}
    }
}
listOf("bootstrap", "update").forEach { type ->
    task<Exec>("carthage${type.capitalize()}") {
        commandLine("$rootDir/gradle/carthage.sh")
        args(
            type,
            "--platform", "iOS",
            "--cache-builds"
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink> {
    dependsOn("carthageBootstrap")
}


// Delete build directory on clean
tasks.named<Delete>("clean") {
    delete(buildDir)
    delete(carthageBuildDir)
}

android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.2")
    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(30)
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs(file("src/androidMain/kotlin"))
            res.srcDirs(file("src/androidMain/res"))
        }
    }
}

