include(
    ":smartype-api",
    ":smartype-generator",
    ":smartype",
    ":smartype-receivers:smartype-mparticle"
)
rootProject.name = "smartype"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
