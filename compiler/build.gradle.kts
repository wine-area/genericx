plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish)
}

group = "io.nanfeng"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // kotlin lombok plugin
    implementation("org.jetbrains.kotlin:kotlin-lombok:1.9.0")
    compileOnly(libs.kotlin.complier.embeddable)
    implementation(projects.core)
    implementation(libs.ksp.api)
    implementation(libs.kotlin.poet)
    implementation(libs.autoService)
    ksp(libs.autoService.ksp)
    testImplementation(libs.kotlinCompileTesting.ksp)
    testImplementation(libs.kotlin.scripting.compiler.embeddable)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}