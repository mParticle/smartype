package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.MessageReceiver
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEvent
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEventType
import com.mparticle.smartype.api.receivers.mparticle.models.ScreenViewEvent
import kotlinx.serialization.json.JsonObject

external class mParticle {
    companion object {
        fun logEvent(name: String, type: Int, attributes: Map<String, String> = definedExternally)
        fun logPageView(name: String, attributes: Map<String, String> = definedExternally)
    }
}

@JsExport
actual class MParticleReceiver : MessageReceiver {
    override fun receive(message: JsonObject) {
        val commonEvent = Converters.convertToEvent(message) ?: return
        if (commonEvent is CustomEvent) {
            val data = commonEvent.data
            if (data != null) {
                val eventType = NativeConverters().convertToNativeEventType(data.custom_event_type)
                val eventName = data.event_name
                val attributes = data.custom_attributes
                if (eventName != null && eventType != null) {
                    if (attributes != null) {
                        mParticle.logEvent(eventName, eventType, attributes)
                    } else {
                        mParticle.logEvent(eventName, eventType)
                    }
                }
            }
        }
        if (commonEvent is ScreenViewEvent) {
            val data = commonEvent.data
            if (data != null) {
                val screenName = data.screen_name
                val attributes = data.custom_attributes
                if (screenName != null) {
                    if (attributes != null) {
                        mParticle.logPageView(screenName, attributes)
                    } else {
                        mParticle.logPageView(screenName)
                    }
                }
            }
        }
    }
    class NativeConverters {
            fun convertToNativeCustomAttributes(customAttributes: Map<String, Any?>): Map<String, String>? {
                val attributes = mutableMapOf<String, String>()
                for ((key, value) in customAttributes) {
                    if (value != null && value != "null") {
                        attributes[key] = value.toString()
                    }
                }
                return attributes
            }

            fun convertToNativeEventType(commonCustomEventType: CustomEventType?): Int? {
                return when (commonCustomEventType) {
                    CustomEventType.navigation -> 1
                    CustomEventType.location -> 2
                    CustomEventType.search -> 3
                    CustomEventType.transaction -> 4
                    CustomEventType.user_content -> 5
                    CustomEventType.user_preference -> 6
                    CustomEventType.social -> 7
                    CustomEventType.other -> 8
                    CustomEventType.media -> 9
                    else -> null
                }
            }
    }

}
