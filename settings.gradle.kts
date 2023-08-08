pluginManagement {
    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    // configure for versioncatelog


}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "genericx"
include("compiler")
include("dependecies")
include("core")
