object versions {
    const val kotlin = "1.4.0"
    const val dokka = "0.10.1"
    const val spotless = "3.27.0"
    const val serialization = "1.0-M1-1.4.0-rc"
}

object deps {
    object android {
        const val gradlePlugin = "com.android.tools.build:gradle:3.6.1"
    }
    object mparticle {
        const val androidSdk = "com.mparticle:android-core:5.12.14"
        const val webSdk = "@mparticle/web-sdk"
    }
    object kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    }
}
