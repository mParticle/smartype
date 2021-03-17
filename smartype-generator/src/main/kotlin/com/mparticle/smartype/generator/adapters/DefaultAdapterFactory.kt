package com.mparticle.smartype.generator.adapters

import com.github.ajalt.clikt.core.CliktError
import com.mparticle.smartype.generator.AnalyticsSchemaAdapter

class DefaultAdapterFactory {
    fun createFromName(apiSchemaType: String?): AnalyticsSchemaAdapter {
        return when (apiSchemaType) {
            GenericAdapter().getName() -> GenericAdapter()
            OpenApiAdapter().getName() -> OpenApiAdapter()
            MParticleDataPlanAdapter().getName(), null -> MParticleDataPlanAdapter()
            else -> null
        }
            ?: throw CliktError(
                "Unsupported apiSchemaType: $apiSchemaType. Check your Smartype configuration. Supported adapters: " + listOf(
                    GenericAdapter().getName(),
                    OpenApiAdapter().getName(),
                    MParticleDataPlanAdapter().getName()
                )
            )
    }
}
