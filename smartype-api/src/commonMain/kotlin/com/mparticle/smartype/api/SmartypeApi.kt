package com.mparticle.smartype.api


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlinx.serialization.modules.SerializersModuleBuilder

public abstract class SmartypeApiBase(private val receivers: kotlin.collections.List<MessageReceiver>) {
    private val mutableReceivers = receivers.toMutableList()


    public fun send(message: Message) {
        val json = Json { isLenient = true }

        mutableReceivers.forEach { it.receive(json.encodeToJsonElement(Message.serializer(), message) as JsonObject) }
    }
    public fun addReceiver(receiver: MessageReceiver): Boolean {
        return mutableReceivers.add(receiver)
    }
    public fun removeReceiver(receiver: MessageReceiver): Boolean {
        return mutableReceivers.remove(receiver)
    }

    public abstract fun getSerializersModule(): SerializersModule;
}
