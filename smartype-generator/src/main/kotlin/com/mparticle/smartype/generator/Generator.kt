package com.mparticle.smartype.generator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.mparticle.smartype.generator.adapters.DefaultAdapterFactory
import com.mparticle.smartype.generator.adapters.MParticleDataPlanAdapter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.util.Objects


class Init : CliktCommand(name="init", help = "Initialize a 'smartype.config.json' configuration file") {
    val ios by option().choice("yes", "no").prompt("Would you like to generate iOS?", default = "no")
    val android by option().choice("yes", "no").prompt("Would you like to generate Android?", default = "no")
    val web by option().choice("yes", "no").prompt("Would you like to generate Web?", default = "no")
    val binaryOutputDirectory by option().prompt("Where should Smartype libraries be generated", default = "smartype-dist")
    val apiSchemaFile by option().prompt("Please specify a file path containing the JSON schema that you'd like to use for generation")

    override fun run() {

        var options = GeneratorOptions(
            IOSOptions(ios == "yes"),
            AndroidOptions(android == "yes"),
            WebOptions(web == "yes"),
            binaryOutputDirectory,
            apiSchemaFile
        )
        val json = Json {
            isLenient = true
            prettyPrint = true
        }
        val jsonData = json.encodeToString(GeneratorOptions.serializer(), options)
        File(System.getProperty("user.dir")).resolve(File("smartype.config.json")).writeText(jsonData)
        echo("smartype.config.json generated successfully! Have fun :-)")

    }
}

class Clean : CliktCommand(name="clean", help = "Remove temporary .smartype directory") {
    override fun run() {
        val directory =
            File(System.getProperty("user.dir")).resolve(File(".smartype"))
        directory.deleteRecursively()
        echo("Done cleaning .smartype directory")
    }
}

class Generate : CliktCommand(name="generate", help = "Generate Smartype Client libraries given a Smartype configuration file") {
    private val config by option(help="A configuration file generated by the init command")
        .file().default(File(System.getProperty("user.dir")).resolve("smartype.config.json"))
    private val gradleProperties: String? by option(help="The gradle system properties to set for the build")

    private val GRADLEW_EXECUTABLE = "gradlew"
    private val TEMP_DIR = ".smartype/"
    private fun jsonContents(file: File): JsonObject {
        val json = Json { }
        return json.parseToJsonElement(file.readText()) as JsonObject
    }

    private fun runningInJar() : Boolean {
        val protocol = this.javaClass.getResource("").protocol
        return Objects.equals(protocol, "jar")
    }

    private fun extractJar(targetDirectory: String) {
        val jarFile = File(
                Generate::class.java.getProtectionDomain().getCodeSource().getLocation()
                        .toURI()
        ).getPath()
        val pb = ProcessBuilder(listOf("unzip","-o", "-q", jarFile, "-d", targetDirectory, "-x", "*.class"))
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        pb.redirectError(ProcessBuilder.Redirect.INHERIT)
        val p = pb.start()
        p.waitFor()
    }

    override fun run() {
        val json = Json { isLenient = true }
        val options = json.decodeFromString<GeneratorOptions>(GeneratorOptions.serializer(), config.readText())

        val inJar = runningInJar()

        if (inJar) {
            extractJar(TEMP_DIR)
        }

        val jsonSchema = jsonContents(File(System.getProperty("user.dir")).resolve(File(options.apiSchemaFile)))
        //TODO: enable additional adapters based on configuration
        val adapter = DefaultAdapterFactory().createFromName(MParticleDataPlanAdapter().getName())
        val analyticsSchema = adapter.extractSchemas(jsonSchema)

        if (options.run { androidOptions.enabled || iosOptions.enabled || webOptions.enabled }) {
            val smartTypeClass = SmartypeObject(options)
            smartTypeClass.configureApi(analyticsSchema)

            var outDirectory = TEMP_DIR + "smartype-generator/build/generatedSources"
            if (!inJar) {
                outDirectory = "build/generatedSources"
            }
            smartTypeClass.finalize(outDirectory)
        }

        var gradleBinDir = TEMP_DIR
        var projectDirectory = TEMP_DIR
        var binOutputDirectory = options.binaryOutputDirectory
        if (!inJar) {
            gradleBinDir = "../"
            projectDirectory = "../"
        }

        val gradleArgs = mutableListOf<String>()
        if (projectDirectory.isNotBlank()) {
            gradleArgs.add("-p")
            gradleArgs.add(projectDirectory)
        }

        if (options.androidOptions.enabled) {
            gradleArgs.add(":smartype:bundleReleaseAar")
        }
        if (options.iosOptions.enabled) {
            gradleArgs.add(":smartype:podPublishXCFramework")
        }
        if (options.webOptions.enabled) {
            gradleArgs.add(":smartype:jsBrowserDistribution")
        }

        //this is used to switch the project dependencies to Maven dependencies
        gradleArgs.add("-PIS_PUBLISHED=true")

        if (gradleProperties != null) {
            gradleArgs.add(gradleProperties!!)
        }

        val gradleCommand = listOf(gradleBinDir + GRADLEW_EXECUTABLE) + gradleArgs
        val pb2 = ProcessBuilder(gradleCommand)
        pb2.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        pb2.redirectError(ProcessBuilder.Redirect.INHERIT)
        val p2 = pb2.start()
        p2.waitFor()

        val directory =
            File(System.getProperty("user.dir")).resolve(File(binOutputDirectory))

        if (!directory.exists()) {
            directory.mkdirs()
        }

        if (options.iosOptions.enabled) {
            val iosBuildDirectory = File(projectDirectory).resolve("smartype/build/cocoapods/publish/release")
            if (iosBuildDirectory.exists()) {
                val mviOS =
                    listOf("mv", iosBuildDirectory.absolutePath, File(binOutputDirectory).resolve("ios/").absolutePath + "/")
                val pb3 = ProcessBuilder(mviOS)
                pb3.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                pb3.redirectError(ProcessBuilder.Redirect.INHERIT)
                val p3 = pb3.start()
                p3.waitFor()
            } else {
                throw CliktError("iOS build failed: unable to locate built binaries in ${iosBuildDirectory.absolutePath}")
            }
        }

        if (options.androidOptions.enabled) {
            val androidBuildDirectory = File(projectDirectory).resolve("smartype/build/outputs/aar")
            if (androidBuildDirectory.exists()) {
                val mvAndroid =
                    listOf(
                        "mv",
                        androidBuildDirectory.absolutePath,
                        File(binOutputDirectory).resolve("android/").absolutePath
                    )
                val pb4 = ProcessBuilder(mvAndroid)
                pb4.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                pb4.redirectError(ProcessBuilder.Redirect.INHERIT)
                val p4 = pb4.start()
                p4.waitFor()
            } else {
                throw CliktError("Android build failed: unable to locate built binaries in ${androidBuildDirectory.absolutePath}")
            }
        }

        if (options.webOptions.enabled) {
            val typescriptDefBuildDir = File(projectDirectory).resolve("build")
            val smartypeBuildDir = File(projectDirectory).resolve("smartype/build/dist/js/productionExecutable")
            if (!File(binOutputDirectory).resolve("web").exists()) {
                File(binOutputDirectory).resolve("web").mkdirs()
            }
            if (smartypeBuildDir.exists()) {

                val mvWeb1 =
                    listOf(
                        "cp",
                        typescriptDefBuildDir.absolutePath + "/js/packages/smartype-smartype/kotlin/smartype-smartype.d.ts",
                        File(binOutputDirectory).resolve("web").absolutePath + "/smartype.d.ts"
                    )
                val pb51 = ProcessBuilder(mvWeb1)
                pb51.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                pb51.redirectError(ProcessBuilder.Redirect.INHERIT)
                val p51 = pb51.start()
                p51.waitFor();

                val mvWeb2 =
                    listOf(
                        "cp",
                        smartypeBuildDir.absolutePath + "/smartype.js",
                        File(binOutputDirectory).resolve("web").absolutePath + "/smartype.js"
                    )
                val pb52 = ProcessBuilder(mvWeb2)
                pb52.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                pb52.redirectError(ProcessBuilder.Redirect.INHERIT)
                val p52 = pb52.start()
                p52.waitFor();
            } else {
                throw CliktError("Web build failed: unable to locate built Web JS distributions in ${smartypeBuildDir.absolutePath}")
            }
        }
    }
}

class Generator: CliktCommand(printHelpOnEmptyArgs = true) {
    override fun run() = Unit
}

fun main(args: Array<String>) = Generator().subcommands(Generate(), Init(), Clean()).main(args)

fun <T> T.`if`(conditional: T.() -> Boolean): T? = if (conditional(this)) this else null
