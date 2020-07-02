import org.jetbrains.kotlin.cli.jvm.main
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version versions.kotlin
    id("com.android.library")
    id("maven-publish")
}

repositories {
    google()
    mavenCentral()
}

val GROUP: String by project
val VERSION_NAME: String by project



group = GROUP
version = VERSION_NAME

val mparticleDir = "$projectDir/../smartype-receivers/smartype-mparticle"
val carthageBuildDir = "$mparticleDir/Carthage/Build/iOS"

kotlin {

        js {
            binaries.executable()
            browser {
            }
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

                cinterops(Action {
                    val mparticleapplesdk by creating {
                        defFile("$mparticleDir/src/iosMain/cinterop/mParticle_Apple_SDK.def")
                        packageName("com.mparticle.applesdk")
                        includeDirs.apply {
                            allHeaders("$carthageBuildDir/mParticle_Apple_SDK.framework/Headers")
                        }
                        compilerOpts("-F$carthageBuildDir/mParticle_Apple_SDK.framework")
                    }
                })
            }
        }
        binaries {
            framework(listOf(RELEASE)) {
                baseName = "Smartype"
                export(project(":smartype-api"))
                export(project(":smartype-receivers:smartype-mparticle"))
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
            kotlin.srcDir("${project(":smartype-generator").buildDir}/generatedSources")
            dependencies {
                api(project(":smartype-api"))
                api(project(":smartype-receivers:smartype-mparticle"))
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${versions.serialization}")
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
            androidMain.dependencies {
                api(kotlin("stdlib"))
            }
        }

        try {
            val jsMain by getting
            if (jsMain != null) {
                jsMain.dependsOn(commonMain)
                jsMain.dependencies {
                    api(kotlin("stdlib-js"))
                }
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
// Delete build directory on clean
tasks.named<Delete>("clean") {
    delete(buildDir)
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
}

