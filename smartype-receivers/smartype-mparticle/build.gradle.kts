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

val carthageBuildDir = "$projectDir/Carthage/Build/iOS"

kotlin {

    android() {
        publishLibraryVariants("release")
        mavenPublication {

            artifactId = "smartype-mparticle"
            pom {
                name.set("mParticle Receiver for Smartype")
                //artifact(tasks["javadocJar"])
                description.set("mParticle Receiver for Smartype")
                url.set("https://github.com/mParticle/smartype")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("samdozor")
                        name.set("Sam Dozor")
                        email.set("sdozor@mparticle.com")
                    }
                    developer {
                        id.set("peterjenkins")
                        name.set("Peter Jenkins")
                        email.set("pjenkins@mparticle.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/mParticle/smartype.git")
                    developerConnection.set("scm:git:ssh://github.com/mParticle/smartype.git")
                    url.set("https://github.com/mParticle/smartype")
                }
            }
        }
    }
    js {
        browser {
            binaries.executable()
        }
    }

    ios() {
        compilations {
            getByName("main") {
                source(sourceSets.getByName("iosMain"))

                kotlinOptions.freeCompilerArgs = listOf("-verbose")

                cinterops(Action {
                    val mparticleapplesdk by creating {
                        defFile("src/iosMain/cinterop/mParticle_Apple_SDK.def")
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
            framework("mParticle_Smartype", listOf(RELEASE)) {
                linkerOpts.add("-F$carthageBuildDir")
                linkerOpts.add("-framework")
                linkerOpts.add("mParticle_Apple_SDK")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":smartype-api"))
                api(kotlin("stdlib-common"))
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

        val iosX64Main by getting {
            dependsOn(commonMain)
        }

        val iosArm64Main by getting {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                api(kotlin("stdlib"))
                api(deps.mparticle.androidSdk)
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }

}

// Create Carthage tasks
listOf("bootstrap", "update").forEach { type ->
    task<Exec>("carthage${type.capitalize()}") {
        group = "carthage"
        executable = "carthage"
        args(
            type,
            "--platform", "iOS",
            "--cache-builds"
        )
    }
}

// Make CInterop tasks depend on Carthage
tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess> {
    dependsOn("carthageBootstrap")
}

// Delete build directory on clean
tasks.named<Delete>("clean") {
    delete(buildDir)
    delete(carthageBuildDir)
}

tasks {
    val javadocJar by creating(Jar::class) {
        dependsOn(dokka)
        archiveClassifier.set("javadoc")
        from(dokka)
    }
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

// Temporary workaround for https://youtrack.jetbrains.com/issue/KT-27170
configurations.create("compileClasspath")

signing {
    if (System.getenv("mavenSigningKeyId") != null) {
        sign(publishing.publications)
    }
}
