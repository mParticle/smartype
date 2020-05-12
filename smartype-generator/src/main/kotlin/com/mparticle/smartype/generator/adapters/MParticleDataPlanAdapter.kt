package com.mparticle.smartype.generator.adapters

import com.mparticle.smartype.generator.AnalyticsSchema
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter
import com.mparticle.smartype.generator.SmartypeObject
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import kotlinx.serialization.json.*

/**
 * Understands the schema of an mParticle Data Plan and adapts it be surfaced by Smartype
 */
class MParticleDataPlanAdapter : AnalyticsSchemaAdapter {

    override fun getName(): String {
        return "mParticle"
    }

    override fun extractSchemas(artifact: JsonObject): AnalyticsSchema {
        println("Extracting mParticle Data Plan")
        val dataPlanId = artifact.getPrimitive("data_plan_id").content
        val dataPlanVersion = artifact.getPrimitive("version").int
        val dataPoints = artifact.getObject("version_document").getArray("data_points")
        println("Found ${dataPoints.size} mParticle data points.")

        val dataPlanIdProperty = PropertySpec.builder("dataPlanId", String::class)
        .initializer(buildCodeBlock {
            add("%S", dataPlanId)
        }).build()
        val dataPlanVersionProperty = PropertySpec.builder("dataPlanVersion", Int::class)
            .initializer(buildCodeBlock {
                add("%L", dataPlanVersion)
            }).build()
        val dataPlanProperties = listOf(dataPlanIdProperty, dataPlanVersionProperty)

        val schemas = ArrayList<JsonObject>()
        for (data in dataPoints) {
            val obj = data.jsonObject
            val match = obj.getObject("match")
            val description = obj.getPrimitive("description").content
            val validator = obj.getObject("validator")

            if (!validator.containsKey("type") ||
                validator["type"] as JsonLiteral != JsonLiteral("json_schema")) {
                continue
            }
            val originalSchema = validator.getObject("definition")
            val schema = enrichCustomEventSchema(match, description, originalSchema)
            schemas.add(schema)
        }

        return AnalyticsSchema(
            dataPlanProperties,
            schemas
        )
    }

    private fun enrichCustomEventSchema(match: JsonObject, description: String, schema: JsonObject): JsonObject {
        var eventType = match["type"]
        val criteria = match["criteria"] as JsonObject
        val schemaMap = schema.toMutableMap()
        val propertiesMap = schemaMap["properties"]?.jsonObject?.toMutableMap()
        val dataMap = propertiesMap?.get("data")?.jsonObject?.toMutableMap()
        val dataPropertiesMap = dataMap?.get("properties")?.jsonObject?.toMutableMap()

        if (eventType == null) {
            eventType = JsonLiteral("custom_event")
        }

        val propertiesValue = mutableMapOf<String, JsonElement>()
        propertiesValue["const"] = eventType
        val propertiesValueMap = propertiesValue.toMap()
        propertiesMap?.set("event_type", JsonObject(propertiesValueMap))

        if (eventType == JsonLiteral("custom_event")) {
            val eventName = criteria["event_name"]
            if (description.isBlank()) {
                schemaMap["description"] = JsonPrimitive("Custom Event with name: $eventName")
            } else {
                schemaMap["description"] = JsonPrimitive(description)
            }
            val customEventType = criteria["custom_event_type"]

            if (eventName != null) {
                val nameField = mutableMapOf<String, JsonElement>()
                nameField["type"] = JsonLiteral("string")
                nameField["const"] = eventName
                dataPropertiesMap?.set("event_name", JsonObject(nameField))
                schemaMap[SmartypeObject.SMARTYPE_OBJECT_NAME] = eventName
            }
            if (customEventType != null) {
                val customEventField = mutableMapOf<String, JsonElement>()
                customEventField["type"] = JsonLiteral("string")
                customEventField["const"] = customEventType
                dataPropertiesMap?.set("custom_event_type", JsonObject(customEventField))
            }
        } else if (eventType == JsonLiteral("screen_view")) {
            val screenName = criteria["screen_name"]

            if (screenName != null) {
                val nameField = mutableMapOf<String, JsonElement>()
                nameField["type"] = JsonLiteral("string")
                nameField["const"] = screenName
                dataPropertiesMap?.set("screen_name", JsonObject(nameField))
                schemaMap[SmartypeObject.SMARTYPE_OBJECT_NAME] = screenName
            }
            if (description.isBlank()) {
                schemaMap["description"] = JsonPrimitive("Screen View Event with name: $screenName")
            } else {
                schemaMap["description"] = JsonPrimitive(description)
            }
        }

        if (dataPropertiesMap != null) {
            dataMap["properties"] = JsonObject(dataPropertiesMap)
        }
        if (dataMap != null) {
            propertiesMap["data"] = JsonObject(dataMap)
        }
        if (propertiesMap != null) {
            schemaMap["properties"] = JsonObject(propertiesMap)
        }

        return JsonObject(schemaMap)
    }

}
