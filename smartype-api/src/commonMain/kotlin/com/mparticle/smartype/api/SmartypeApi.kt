package com.mparticle.smartype.api

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
public abstract class SmartypeApiBase() {
    private val mutableReceivers = kotlin.collections.mutableListOf<MessageReceiver>()

    @JsName("send")
    public fun send(message: Message) {
        val messageJson = message.toJson()
        mutableReceivers.forEach { it.receive(messageJson) }
    }
    @JsName("addReceiver")
    public fun addReceiver(receiver: MessageReceiver): Boolean {
        return mutableReceivers.add(receiver)
    }
    @JsName("removeReceiver")
    public fun removeReceiver(receiver: MessageReceiver): Boolean {
        return mutableReceivers.remove(receiver)
    }

}
