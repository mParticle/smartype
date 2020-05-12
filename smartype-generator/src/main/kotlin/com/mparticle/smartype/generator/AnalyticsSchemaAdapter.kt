package com.mparticle.smartype.generator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

interface AnalyticsSchemaAdapter {
    fun getName() : String
    fun extractSchemas(artifact: JsonObject): AnalyticsSchema
}
