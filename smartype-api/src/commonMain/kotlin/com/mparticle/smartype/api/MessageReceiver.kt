package com.mparticle.smartype.api

import kotlinx.serialization.json.JsonObject
import kotlin.js.JsExport

@JsExport
public interface MessageReceiver {
    public fun receive(message: JsonObject)
}
