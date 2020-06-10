package com.mparticle.smartype.api

import kotlinx.serialization.json.JsonObject

public interface MessageReceiver {
    public fun receive(message: JsonObject)
}
