plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}
java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}