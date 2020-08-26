package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.MessageReceiver
import com.mparticle.applesdk.*
import platform.Foundation.NSLog
import kotlinx.serialization.json.JsonObject
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEvent
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEventType
import com.mparticle.smartype.api.receivers.mparticle.models.ScreenViewEvent
import kotlin.native.concurrent.freeze

actual class MParticleReceiver : MessageReceiver {
    override fun receive(message: String) {
        val commonEvent = Converters.convertToEvent(message) ?: return
        if (commonEvent is CustomEvent) {
            val event = NativeConverters.convertToNativeCustomEvent(commonEvent)
            if (event != null) {
                MParticle.sharedInstance().logEvent(event)
            }
        }
        if (commonEvent is ScreenViewEvent) {
            val event = NativeConverters.convertToNativeScreenViewEvent(commonEvent)
            if (event != null) {
                MParticle.sharedInstance().logScreenEvent(event)
            }
        }
    }

    class NativeConverters {
        companion object {
            private fun convertToNativeCustomAttributes(customAttributes: Map<String, Any?>): Map<Any?, *>? {
                val attributes = mutableMapOf<Any?, Any?>()
                for ((key, value) in customAttributes) {
                    if (value != null && value != "null") {
                        attributes[key] = value
                    }
                }
                if (attributes.count() > 0) {
                    return attributes.freeze()
                }
                return null
            }

            fun convertToNativeCustomEvent(commonEvent: CustomEvent) : MPEvent? {
                val data = commonEvent.data

                val customAttributes = data?.custom_attributes
                val commonType = data?.custom_event_type
                val name = data?.event_name
                val type = convertToNativeEventType(commonType)
                if (name == null || type == null) {
                    return null
                }
                val event = MPEvent(name, type)
                val attributes = customAttributes?.let { convertToNativeCustomAttributes(it) }
                if (attributes != null) {
                    event.customAttributes = attributes
                }
                return event
            }

            fun convertToNativeScreenViewEvent(commonEvent: ScreenViewEvent) : MPEvent? {
                val data = commonEvent.data
                val customAttributes = data?.custom_attributes

                val name = commonEvent.data?.screen_name ?: return null
                val type = MPEventTypeNavigation
                val event = MPEvent(name, type)
                val attributes = customAttributes?.let { convertToNativeCustomAttributes(it) }
                if (attributes != null) {
                    event.customAttributes = attributes
                }
                return event
            }

            fun convertToNativeEventType(commonCustomEventType: CustomEventType?): MPEventType? {
                return when (commonCustomEventType) {
                    CustomEventType.navigation -> MPEventTypeNavigation
                    CustomEventType.location -> MPEventTypeLocation
                    CustomEventType.search -> MPEventTypeSearch
                    CustomEventType.transaction -> MPEventTypeTransaction
                    CustomEventType.user_content -> MPEventTypeUserContent
                    CustomEventType.user_preference -> MPEventTypeUserPreference
                    CustomEventType.social -> MPEventTypeSocial
                    CustomEventType.media -> MPEventTypeMedia
                    CustomEventType.other -> MPEventTypeOther
                    else -> null
                }
            }
        }
    }
}
