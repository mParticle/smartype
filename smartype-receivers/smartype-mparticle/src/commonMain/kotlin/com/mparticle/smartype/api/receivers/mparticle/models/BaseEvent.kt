package com.mparticle.smartype.api.receivers.mparticle.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

abstract class BaseEvent() {

    var event_type: EventType? = null
}

@Serializable
class BaseEventAdapter() {
    @SerialName("event_type")
    lateinit var event_type: EventType
}

@Serializable
enum class EventType {
    custom_event,
    screen_view
}
