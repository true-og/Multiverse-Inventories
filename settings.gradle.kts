pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.gradleup.shadow") {
                useModule("org.gradleup:shadow:${requested.version}")
            }
        }
    }
}

rootProject.name = "Multiverse-Inventories"

