package com.mparticle.smartype.generator

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject

/**
 * A Smartype configuration file
 *
 * @param iosOptions The options for generating an iOS-compatible library, default disabled
 * @param androidOptions The options for generating an Android-compatible library, default disabled
 * @param webOptions The options for generating a web-compatible library, default disabled
 * @param binaryOutputDirectory The parent directory to place generated Smartype libraries, default "smartype-dist"
 * @param apiSchemaFile The file containing the JSON schema of the intended API
 * @param dedupEnums Whether multiple classes should share the same enums (vs. prefixing with class name), default false
 */
@Serializable
data class GeneratorOptions(var iosOptions: IOSOptions = IOSOptions(false),
                            var androidOptions: AndroidOptions = AndroidOptions(false),
                            var webOptions: WebOptions = WebOptions(false),
                            var binaryOutputDirectory: String = "smartype-dist",
                            var apiSchemaFile: String,
                            var dedupEnums: Boolean = false
)

@Serializable
data class AndroidOptions(var enabled: Boolean)

@Serializable
data class IOSOptions(var enabled: Boolean)

@Serializable
data class WebOptions(var enabled: Boolean)
