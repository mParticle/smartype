import com.mparticle.smartype.api.receivers.mparticle.MParticleReceiver

/**
 * This file, which is at the "root" package, makes it so that Web developers
 * don't have to refer to MParticleReceiver using its full package name:
 *
 * eg rather than the following JS:
 * let foo = com.mparticle.smartype.api.receivers.mparticle.MParticleReceiver()
 *
 * With this file they can write:
 * let foo = mParticleReceiver()
 */
@JsExport
fun mParticleReceiver(): MParticleReceiver {
    return MParticleReceiver()
}
