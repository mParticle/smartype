package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.receivers.mparticle.models.BaseEvent
import com.mparticle.smartype.api.receivers.mparticle.models.BaseEventAdapter
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEvent
import com.mparticle.smartype.api.receivers.mparticle.models.EventType
import com.mparticle.smartype.api.receivers.mparticle.models.ScreenViewEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

class Converters {
    companion object {
        fun convertToEvent(message: String): BaseEvent? {
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            }
            val adapter = json.decodeFromString(BaseEventAdapter.serializer(), message)
            val type = adapter.event_type
            if (type == EventType.custom_event) {
                return json.decodeFromString(CustomEvent.serializer(), message)
            } else if (type == EventType.screen_view) {
                return json.decodeFromString(ScreenViewEvent.serializer(), message)
            }
            return null
        }

        fun convertToNativeCustomAttributes(customAttributes: Map<String, Any?>): Map<String, String>? {
            val attributes = mutableMapOf<String, String>()
            for ((key, value) in customAttributes) {
                if (value != null && value != "null") {
                    attributes[key] = (value as JsonPrimitive).content
                }
            }
            return attributes
        }
    }
}
