package com.mparticle.smartype.generator.adapters

import com.mparticle.smartype.generator.AnalyticsSchema
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter
import com.mparticle.smartype.generator.SmartypeObject
import com.mparticle.smartype.generator.adapters.MParticleDataPlanAdapter.EventType.*
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import kotlinx.serialization.json.*

/**
 * Understands the schema of an mParticle Data Plan and adapts it be surfaced by Smartype
 */
class MParticleDataPlanAdapter : AnalyticsSchemaAdapter {

    private enum class EventType(val jsonLiteral: JsonLiteral) {
        CommerceEvent(JsonLiteral("commerce_event")),
        CustomEvent(JsonLiteral("custom_event")),
        ScreenView(JsonLiteral("screen_view"))
    }

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
        var type = match["type"]
        val criteria = match["criteria"] as JsonObject
        val schemaMap = schema.toMutableMap()
        val propertiesMap = schemaMap["properties"]?.jsonObject?.toMutableMap()
        val dataMap = propertiesMap?.get("data")?.jsonObject?.toMutableMap()
        val dataPropertiesMap = dataMap?.get("properties")?.jsonObject?.toMutableMap()

        class Entry(val eventType: EventType, val name: JsonElement?, val nameField: String, val defaultDescription: (JsonElement?) -> String)

        val entry: Entry = when (type) {
            null, CustomEvent.jsonLiteral -> {
                criteria["custom_event_type"]?.let { customEventType ->
                    val customEventField = mutableMapOf<String, JsonElement>()
                    customEventField["type"] = JsonLiteral("string")
                    customEventField["const"] = customEventType
                    dataPropertiesMap?.set("custom_event_type", JsonObject(customEventField))
                }
                Entry(CustomEvent, criteria["event_name"], "event_name", {"Custom Event with name: $it"})
            }
            ScreenView.jsonLiteral -> Entry(ScreenView, criteria["screen_name"], "screen_name", {"Screen View Event with name: $it"})
            JsonLiteral("product_action") -> Entry(CommerceEvent, criteria["action"], "product_action", {"Commerce Event with product action: $it"})
            JsonLiteral("promotion_action") -> Entry(CommerceEvent, criteria["action"], "promotion_action", {"Commerce Event with promotion action: $it"})
            JsonLiteral("product_impression") -> Entry(CommerceEvent, criteria["action"], "product_impression", {"Commerce Event with product impression: $it"})
            else -> {
                println("Unable to process Event Type: $type")
                Entry(CustomEvent, JsonPrimitive(""), "", {""})
            }
        }

        entry.name?.let { eventName ->
            val nameField = mutableMapOf<String, JsonElement>()
            nameField["type"] = JsonLiteral("string")
            nameField["const"] = eventName
            dataPropertiesMap?.set(entry.nameField, JsonObject(nameField))
            schemaMap[SmartypeObject.SMARTYPE_OBJECT_NAME] = eventName
        }

        schemaMap["description"] = if (description.isBlank()) {
            JsonPrimitive(entry.defaultDescription(entry.eventType.jsonLiteral))
        } else {
            JsonPrimitive(description)
        }


        val propertiesValue = mutableMapOf<String, JsonElement>()
        propertiesValue["const"] = entry.eventType.jsonLiteral
        val propertiesValueMap = propertiesValue.toMap()
        propertiesMap?.set("event_type", JsonObject(propertiesValueMap))

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
