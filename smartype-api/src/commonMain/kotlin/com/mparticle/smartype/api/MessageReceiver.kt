package com.mparticle.smartype.api


public expect interface MessageReceiver {
    public fun receive(message: String)
}