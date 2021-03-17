package com.mparticle.smartype.generator.adapters

import com.mparticle.smartype.generator.AnalyticsSchema
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter
import com.mparticle.smartype.generator.SmartypeObject
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import kotlinx.serialization.json.*

class OpenApiAdapter : AnalyticsSchemaAdapter {
    override fun getName(): String {
        return "openapi"
    }

    override fun extractSchemas(artifact: JsonObject): AnalyticsSchema {
        println("Extracting Open API spec")
        val title = artifact["info"]?.jsonObject?.get("title")?.jsonPrimitive?.toString()
        val version = artifact["info"]?.jsonObject?.get("version")?.jsonPrimitive?.toString()
        val properties = mutableListOf<PropertySpec>()

        if (title != null) {
            val openApiTitleProperty = PropertySpec.builder("title", String::class)
                .addModifiers(KModifier.PUBLIC)
                .initializer(buildCodeBlock {
                    add("%S", title)
                }).build()
            properties.add(openApiTitleProperty)
        }
        if (version != null) {
            val openApiVersionProperty = PropertySpec.builder("version", String::class)
                .addModifiers(KModifier.PUBLIC)
                .initializer(buildCodeBlock {
                    add("%S", version)
                }).build()
            properties.add(openApiVersionProperty)
        }


        val schemas = artifact["components"]?.jsonObject?.get("schemas")?.jsonObject
        if (schemas != null){
            val schemaList = schemas.map {
                val schema = it.value.jsonObject.toMutableMap()
                schema.put(SmartypeObject.SMARTYPE_OBJECT_NAME, JsonPrimitive(it.key))
                JsonObject(schema)
            }.toList()
            return AnalyticsSchema(
                properties,
                schemaList
            )
        }
        return AnalyticsSchema(
            properties,
            listOf()
        )
    }
}
