import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.nanfeng"

repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile> {
    val pluginId = "io.nanfeng.genericx"
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-P",
            "plugin:$pluginId:tag=genericx"
        )
    }

}


dependencies {
    PLUGIN_CLASSPATH_CONFIGURATION_NAME(projects.compiler)
    implementation(projects.core)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}