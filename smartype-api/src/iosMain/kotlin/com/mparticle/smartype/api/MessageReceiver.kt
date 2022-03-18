package com.mparticle.smartype.api

public actual interface MessageReceiver {
    public actual fun receive(message: String)
}