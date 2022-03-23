package com.mparticle.smartype.api

@JsExport
public actual external interface Serializable {
    @JsName("toJson")
    public actual fun toJson(): String
}