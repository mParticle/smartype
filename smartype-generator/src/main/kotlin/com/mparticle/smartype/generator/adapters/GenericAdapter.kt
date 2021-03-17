package com.mparticle.smartype.generator.adapters

import com.mparticle.smartype.generator.AnalyticsSchema
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GenericAdapter : AnalyticsSchemaAdapter {
    override fun getName(): String {
        return "generic"
    }

    override fun extractSchemas(artifact: JsonObject): AnalyticsSchema {
        println("Extracting generic JSON schema spec")
        val id = artifact["\$id"]?.jsonPrimitive?.toString()
        val schema = artifact["\$schema"]?.jsonPrimitive?.toString()

        val properties = mutableListOf<PropertySpec>()

        if (id != null) {
            val idProperty = PropertySpec.builder("id", String::class)
                .addModifiers(KModifier.PUBLIC)
                .initializer(buildCodeBlock {
                    add("%S", id)
                }).build()
            properties.add(idProperty)
        }
        if (schema != null) {
            val schemaProperty = PropertySpec.builder("schema", String::class)
                .addModifiers(KModifier.PUBLIC)
                .initializer(buildCodeBlock {
                    add("%S", schema)
                }).build()
            properties.add(schemaProperty)
        }

        return AnalyticsSchema(
            properties,
            listOf(artifact)
        )
    }
}
