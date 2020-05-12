package com.mparticle.smartype.api


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerialModuleCollector
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder

abstract class SmartypeApiBase(private val receivers: kotlin.collections.List<MessageReceiver>) {
    private val mutableReceivers = receivers.toMutableList()


    fun send(message: Message) {
        var config = JsonConfiguration(
            encodeDefaults = true,
            ignoreUnknownKeys = true,
            isLenient = true,
            serializeSpecialFloatingPointValues = false,
            allowStructuredMapKeys = true,
            prettyPrint = false,
            unquotedPrint = false,
            useArrayPolymorphism = false
        )
        val json = Json(
            configuration = config,
            context = getSerialModule()
        )

        mutableReceivers.forEach { it.receive(json.toJson(Message.serializer(), message) as JsonObject) }
    }
    fun addReceiver(receiver: MessageReceiver): Boolean {
        return mutableReceivers.add(receiver)
    }
    fun removeReceiver(receiver: MessageReceiver): Boolean {
        return mutableReceivers.remove(receiver)
    }

    abstract fun getSerialModule(): SerialModule;
}
