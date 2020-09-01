package com.mparticle.smartype.api.receivers.mparticle.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
@SerialName("custom_event")
class CustomEvent: BaseEvent() {
    var data: CustomEventData? = null
    init {
        event_type = EventType.custom_event
    }
}

@Serializable
class CustomEventData {
    var event_name: String? = null
    var custom_event_type: CustomEventType? = null
    var custom_attributes: Map<String, JsonPrimitive>? = null
}

@Serializable
@SerialName("custom_event_type")
enum class CustomEventType {
    @SerialName("navigation")
    navigation,
    @SerialName("location")
    location,
    @SerialName("search")
    search,
    @SerialName("transaction")
    transaction,
    @SerialName("user_content")
    user_content,
    @SerialName("user_preference")
    user_preference,
    @SerialName("social")
    social,
    @SerialName("other")
    other,
    @SerialName("media")
    media
}
