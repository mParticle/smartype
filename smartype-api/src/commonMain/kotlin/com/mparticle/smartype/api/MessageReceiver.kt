package com.mparticle.smartype.api

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
public interface MessageReceiver {
    @JsName("receive")
    public fun receive(message: String)
}
