package com.mparticle.smartype.api

@JsExport
public actual external interface MessageReceiver {
    @JsName("receive")
    public actual fun receive(message: String)
}