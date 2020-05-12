package com.mparticle.smartype.generator

import com.squareup.kotlinpoet.PropertySpec
import kotlinx.serialization.json.JsonObject

data class AnalyticsSchema(val smartypeApiPublicProperties: List<PropertySpec>,
                           val smartypeMessageSchemas: List<JsonObject>)
