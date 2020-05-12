package com.mparticle.smartype.api.receivers.mparticle.models

import kotlinx.serialization.Serializable

@Serializable
class ScreenViewEvent: BaseEvent() {
    var data: ScreenViewEventData? = null
    init {
        event_type = EventType.screen_view
    }
}

@Serializable
class ScreenViewEventData {
    var screen_name: String? = null
    var custom_attributes: Map<String, String>? = null
}
