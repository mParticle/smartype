package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.MPEvent
import com.mparticle.MParticle
import com.mparticle.smartype.api.MessageReceiver
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEvent
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEventType
import com.mparticle.smartype.api.receivers.mparticle.models.ScreenViewEvent
import kotlinx.serialization.json.JsonObject

actual class MParticleReceiver : MessageReceiver {
    override fun receive(message: String) {
        val commonEvent = Converters.convertToEvent(message) ?: return
        if (commonEvent is CustomEvent) {
            val event = NativeConverters.convertToNativeCustomEvent(commonEvent)
            if (event != null) {
                MParticle.getInstance()?.logEvent(event)
            }
        }
        if (commonEvent is ScreenViewEvent) {
            val event = NativeConverters.convertToNativeScreenViewEvent(commonEvent)
            if (event != null) {
                MParticle.getInstance()?.logScreen(event)
            }
        }
    }
    class NativeConverters {
        companion object {
            fun convertToNativeCustomAttributes(customAttributes: Map<String, Any?>): Map<String, String>? {
                val attributes = mutableMapOf<String, String>()
                for ((key, value) in customAttributes) {
                    if (value != null && value != "null") {
                        attributes[key] = value.toString()
                    }
                }
                return attributes
            }

            fun convertToNativeCustomEvent(commonEvent: CustomEvent): MPEvent? {
                val data = commonEvent.data

                val customAttributes = data?.custom_attributes
                val commonType = data?.custom_event_type
                val name = data?.event_name
                val type = convertToNativeEventType(commonType)
                if (name == null || type == null) {
                    return null
                }
                val event = MPEvent.Builder(name, type)
                val attributes = customAttributes?.let { convertToNativeCustomAttributes(it) }
                if (attributes != null) {
                    event.customAttributes(attributes)
                }
                return event.build()
            }

            fun convertToNativeScreenViewEvent(commonEvent: ScreenViewEvent): MPEvent? {
                val data = commonEvent.data
                val customAttributes = data?.custom_attributes

                val name = commonEvent.data?.screen_name ?: return null
                val type = MParticle.EventType.Navigation
                val event = MPEvent.Builder(name, type)
                val attributes = customAttributes?.let { convertToNativeCustomAttributes(it) }
                if (attributes != null) {
                    event.customAttributes(attributes)
                }
                return event.build()
            }

            fun convertToNativeEventType(commonCustomEventType: CustomEventType?): MParticle.EventType? {
                return when (commonCustomEventType) {
                    CustomEventType.navigation -> MParticle.EventType.Navigation
                    CustomEventType.location -> MParticle.EventType.Location
                    CustomEventType.search -> MParticle.EventType.Search
                    CustomEventType.transaction -> MParticle.EventType.Transaction
                    CustomEventType.user_content -> MParticle.EventType.UserContent
                    CustomEventType.user_preference -> MParticle.EventType.UserPreference
                    CustomEventType.social -> MParticle.EventType.Social
                    CustomEventType.media -> MParticle.EventType.Media
                    CustomEventType.other -> MParticle.EventType.Other
                    else -> null
                }
            }
        }
    }
}
