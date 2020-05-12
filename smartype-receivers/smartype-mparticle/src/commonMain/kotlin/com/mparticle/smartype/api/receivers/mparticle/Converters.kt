package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.Message
import com.mparticle.smartype.api.receivers.mparticle.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class Converters {
    companion object {
        fun convertToEvent(message: JsonObject) : BaseEvent? {
            var config = JsonConfiguration(
                encodeDefaults = true,
                ignoreUnknownKeys = true,
                isLenient = true,
                serializeSpecialFloatingPointValues = false,
                allowStructuredMapKeys = true,
                prettyPrint = false,
                unquotedPrint = false,
                useArrayPolymorphism = false
            )
            val json = Json(config)
            val adapter = json.fromJson(BaseEventAdapter.serializer(), message)
            val type = adapter.event_type

            if (type == EventType.custom_event) {
                return json.fromJson<CustomEvent>(CustomEvent.serializer(), message)
            } else if (type == EventType.screen_view) {
                return json.fromJson<ScreenViewEvent>(ScreenViewEvent.serializer(), message)
            }
            return null
        }

    }
}
