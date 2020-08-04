package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.Message
import com.mparticle.smartype.api.receivers.mparticle.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class Converters {
    companion object {
        fun convertToEvent(message: JsonObject) : BaseEvent? {
            val json = Json { isLenient = true }
            val adapter = json.decodeFromJsonElement(BaseEventAdapter.serializer(), message)

            val type = adapter.event_type

            if (type == EventType.custom_event) {
                return json.decodeFromJsonElement<CustomEvent>(CustomEvent.serializer(), message)
            } else if (type == EventType.screen_view) {
                return json.decodeFromJsonElement<ScreenViewEvent>(ScreenViewEvent.serializer(), message)
            }
            return null
        }

    }
}
