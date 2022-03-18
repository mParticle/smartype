package com.mparticle.smartype.api

import kotlin.js.JsExport
import kotlinx.serialization.json.JsonObject
import kotlin.js.JsName

public expect interface Serializable {
    public fun toJson(): String
}