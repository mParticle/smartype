package com.mparticle.smartype.api

import kotlinx.serialization.json.JsonObject

interface MessageReceiver {
    fun receive(message: JsonObject)
}
