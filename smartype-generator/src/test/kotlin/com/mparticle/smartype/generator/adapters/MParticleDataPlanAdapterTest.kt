package com.mparticle.smartype.generator.adapters

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MParticleDataPlanAdapterTest {
    @Test
    fun testExtractPublicProperties() {

        val path = "src/test/resources/mparticle/freddysPlan.json"
        val planObject = loadFileAsJson(path)
        val result = MParticleDataPlanAdapter().extractSchemas(planObject)
        assertEquals(2, result.smartypeApiPublicProperties.size)

        val idProp = result.smartypeApiPublicProperties.find { it.name == "dataPlanId" }
        assertNotNull(idProp)
        val versionProp = result.smartypeApiPublicProperties.find { it.name == "dataPlanVersion" }
        assertNotNull(versionProp)

    }
    private fun loadFileAsJson(path: String): JsonObject {
        val file = File(path)
        val freddyString = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return Json.parseToJsonElement(freddyString).jsonObject
    }

    @Test
    fun testExtractDataPoints() {
        val path = "src/test/resources/mparticle/freddysPlan.json"
        val planObject = loadFileAsJson(path)
        val result = MParticleDataPlanAdapter().extractSchemas(planObject)
        assertEquals(16, result.smartypeMessageSchemas.size)
    }
}