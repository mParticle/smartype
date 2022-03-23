package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.MessageReceiver
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEvent
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEventType
import com.mparticle.smartype.api.receivers.mparticle.models.ScreenViewEvent
import kotlinx.browser.window
import org.w3c.dom.get

external class mParticle {
        fun logEvent(name: String, type: Int = definedExternally, attributes: Any = definedExternally)
        fun logPageView(name: String, attributes: Any = definedExternally)
}

@JsExport
actual class MParticleReceiver : MessageReceiver {

override fun receive(message: String) {
        console.log("MParticleReceiver#receive:message=$message")

        val commonEvent = Converters.convertToEvent(message) ?: return

        var mParticle: mParticle = window["mParticle"]

        if (commonEvent is CustomEvent) {
            val data = commonEvent.data
            if (data != null) {
                val eventType = NativeConverters().convertToNativeEventType(data.custom_event_type)
                val eventName = data.event_name
                val attributes = data.custom_attributes

                if (eventName != null && eventType != null) {
                    if (attributes != null) {
                        var attributesString = "{"
                        for ((key, value) in attributes) {
                            if (value.isString) {
                                val content = value.content
                                attributesString += "\"$key\":\"$content\","
                            } else {
                                attributesString += "\"$key\":$value,"
                            }
                        }
                        attributesString = attributesString.dropLast(1)
                        attributesString += "}"
                        mParticle.logEvent(eventName, eventType, JSON.parse(attributesString))
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
                        var attributesString = "{"
                        for ((key, value) in attributes) {
                            if (value.isString) {
                                val content = value.content
                                attributesString += "\"$key\":\"$content\","
                            } else {
                                attributesString += "\"$key\":$value,"
                            }
                        }
                        attributesString = attributesString.dropLast(1)
                        attributesString += "}"
                        mParticle.logPageView(screenName, JSON.parse(attributesString))
                    } else {
                        mParticle.logPageView(screenName)
                    }
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
