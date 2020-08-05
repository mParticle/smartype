package com.mparticle.smartype.generator.adapters

import com.mparticle.smartype.generator.AnalyticsSchema
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter
import com.mparticle.smartype.generator.SmartypeObject
import com.squareup.kotlinpoet.KModifier
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
        val dataPlanId = artifact["data_plan_id"]!!.jsonPrimitive.content
        val dataPlanVersion = artifact["version"]!!.jsonPrimitive.int
        val dataPoints = artifact["version_document"]!!.jsonObject["data_points"]!!.jsonArray
        println("Found ${dataPoints.size} mParticle data points.")

        val dataPlanIdProperty = PropertySpec.builder("dataPlanId", String::class)
            .addModifiers(KModifier.PUBLIC)
            .initializer(buildCodeBlock {
                add("%S", dataPlanId)
            }).build()
        val dataPlanVersionProperty = PropertySpec.builder("dataPlanVersion", Int::class)
            .addModifiers(KModifier.PUBLIC)
            .initializer(buildCodeBlock {
                add("%L", dataPlanVersion)
            }).build()
        val dataPlanProperties = listOf(dataPlanIdProperty, dataPlanVersionProperty)

        val schemas = ArrayList<JsonObject>()
        for (data in dataPoints) {
            val obj = data.jsonObject
            val match = obj["match"]!!.jsonObject
            val description = obj["description"]!!.jsonPrimitive.content
            val validator = obj["validator"]!!.jsonObject

            if (!validator.containsKey("type") ||
                (validator["type"] as JsonPrimitive).content != "json_schema") {
                continue
            }
            val originalSchema = validator["definition"]!!.jsonObject
            val schema = enrichCustomEventSchema(match, description, originalSchema)
            schemas.add(schema)
        }

        return AnalyticsSchema(
            dataPlanProperties,
            schemas
        )
    }

    private fun enrichCustomEventSchema(match: JsonObject, description: String, schema: JsonObject): JsonObject {
        var eventType = match["type"]?.jsonPrimitive?.content
        val criteria = match["criteria"] as JsonObject
        val schemaMap = schema.toMutableMap()
        val propertiesMap = schemaMap["properties"]?.jsonObject?.toMutableMap()
        val dataMap = propertiesMap?.get("data")?.jsonObject?.toMutableMap()
        val dataPropertiesMap = dataMap?.get("properties")?.jsonObject?.toMutableMap()

        if (eventType == null) {
            eventType = "custom_event"
        }

        val propertiesValue = mutableMapOf<String, JsonElement>()
        propertiesValue["const"] = JsonPrimitive(eventType)
        val propertiesValueMap = propertiesValue.toMap()
        propertiesMap?.set("event_type", JsonObject(propertiesValueMap))

        if (eventType == "custom_event") {
            val eventName = criteria["event_name"]
            if (description.isBlank()) {
                schemaMap["description"] = JsonPrimitive("Custom Event with name: $eventName")
            } else {
                schemaMap["description"] = JsonPrimitive(description)
            }
            val customEventType = criteria["custom_event_type"]

            if (eventName != null) {
                val nameField = mutableMapOf<String, JsonElement>()
                nameField["type"] = JsonPrimitive(value = "string")
                nameField["const"] = eventName
                dataPropertiesMap?.set("event_name", JsonObject(nameField))
                schemaMap[SmartypeObject.SMARTYPE_OBJECT_NAME] = eventName
            }
            if (customEventType != null) {
                val customEventField = mutableMapOf<String, JsonElement>()
                customEventField["type"] = JsonPrimitive(value = "string")
                customEventField["const"] = customEventType
                dataPropertiesMap?.set("custom_event_type", JsonObject(customEventField))
            }
        } else if (eventType == "screen_view") {
            val screenName = criteria["screen_name"]

            if (screenName != null) {
                val nameField = mutableMapOf<String, JsonElement>()
                nameField["type"] = JsonPrimitive(value = "string")
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
