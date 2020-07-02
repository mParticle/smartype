package com.mparticle.smartype.api.receivers.mparticle

import com.mparticle.smartype.api.Message
import com.mparticle.smartype.api.MessageReceiver
import com.mparticle.smartype.api.receivers.mparticle.models.CustomEvent
import kotlin.js.JsExport

@JsExport
expect class MParticleReceiver : MessageReceiver {

}
