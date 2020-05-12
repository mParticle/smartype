package com.mparticle.smartype.generator.adapters

import com.mparticle.smartype.generator.AnalyticsSchema
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class GenericAdapter : AnalyticsSchemaAdapter {
    override fun getName(): String {
        return "generic"
    }

    override fun extractSchemas(artifact: JsonObject): AnalyticsSchema {
        TODO("Smartype doesn't support generic schemas yet!")
    }
}
