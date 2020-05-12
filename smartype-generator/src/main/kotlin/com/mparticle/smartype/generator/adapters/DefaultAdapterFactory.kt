package com.mparticle.smartype.generator.adapters

class DefaultAdapterFactory {
    fun createFromName(name: String) = when (name) {
        "mParticle" -> MParticleDataPlanAdapter()
        else -> GenericAdapter()
    }
}
