package com.mparticle.smartype.api

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
public interface Serializable {
    @JsName("toJson")
    public fun toJson(): String
}
