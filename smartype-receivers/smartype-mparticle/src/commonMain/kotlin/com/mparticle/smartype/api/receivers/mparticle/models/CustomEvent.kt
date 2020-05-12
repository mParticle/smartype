package com.mparticle.smartype.api.receivers.mparticle.models

import kotlinx.serialization.Serializable

@Serializable
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
    var custom_attributes: Map<String, String>? = null
}

@Serializable
enum class CustomEventType {
    navigation,
    location,
    search,
    transaction,
    user_content,
    user_preference,
    social,
    other,
    media
}
