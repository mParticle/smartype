package com.mparticle.smartype.api.receivers.mparticle.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

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
    @SerialName("custom_event")
    custom_event,
    @SerialName("screen_view")
    screen_view
}
