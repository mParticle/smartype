package com.mparticle.smartype

import mParticleReceiver
import com.mparticle.smartype.api.receivers.mparticle.MParticleReceiver

@JsExport
actual class Constants {
    actual fun platform(): String {
        //this forces the resulting JS distribution to contain mParticleReceiver
        var receiver: MParticleReceiver = mParticleReceiver()
        return "js"
    }
}
