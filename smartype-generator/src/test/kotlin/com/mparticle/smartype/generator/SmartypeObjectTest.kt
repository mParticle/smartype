package com.mparticle.smartype.generator

import com.mparticle.smartype.generator.adapters.DefaultAdapterFactory
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class SmartypeObjectTest {

    private fun loadFileAsJson(path: String): JsonObject {
        val file = File(path)
        val freddyString = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return Json.parseToJsonElement(freddyString).jsonObject
    }

    @Test
    fun testmParticleDataPlan() {
        val options = GeneratorOptions(
            webOptions = WebOptions(enabled = false),
            apiSchemaFile = "foo",
            apiSchemaType = "mparticle"
        )
        val path = "src/test/resources/mparticle/freddysPlan.json"
        val planObject = loadFileAsJson(path)
        val adapter = DefaultAdapterFactory().createFromName(options.apiSchemaType)
        val analyticsSchema = adapter.extractSchemas(planObject)
        val smartTypeClass = SmartypeObject(options)
        val file = smartTypeClass.configureApi(analyticsSchema)
        val fileString = StringBuilder()
        file.writeTo(fileString)
        val kotlinSource = SourceFile.kotlin(file.name + ".kt", fileString.toString())
        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            inheritClassPath = true
            messageOutputStream = System.out
            useIR = true
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun testOpenApiPlan() {
        val options = GeneratorOptions(
            webOptions = WebOptions(enabled = false),
            apiSchemaFile = "foo",
            apiSchemaType = "openapi"
        )
        val path = "src/test/resources/openapi/petstore.json"
        val planObject = loadFileAsJson(path)
        val adapter = DefaultAdapterFactory().createFromName(options.apiSchemaType)
        val analyticsSchema = adapter.extractSchemas(planObject)
        val smartTypeClass = SmartypeObject(options)
        val file = smartTypeClass.configureApi(analyticsSchema)
        val fileString = StringBuilder()
        file.writeTo(fileString)
        val kotlinSource = SourceFile.kotlin(file.name + ".kt", fileString.toString())
        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            inheritClassPath = true
            messageOutputStream = System.out
            useIR = true
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun testGenericSchema() {
        val options = GeneratorOptions(
            webOptions = WebOptions(enabled = false),
            apiSchemaFile = "foo",
            apiSchemaType = "generic"
        )
        val path = "src/test/resources/generic/person.json"
        val planObject = loadFileAsJson(path)
        val adapter = DefaultAdapterFactory().createFromName(options.apiSchemaType)
        val analyticsSchema = adapter.extractSchemas(planObject)
        val smartTypeClass = SmartypeObject(options)
        val file = smartTypeClass.configureApi(analyticsSchema)
        val fileString = StringBuilder()
        file.writeTo(fileString)
        val kotlinSource = SourceFile.kotlin(file.name + ".kt", fileString.toString())
        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            inheritClassPath = true
            messageOutputStream = System.out
            useIR = true
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}