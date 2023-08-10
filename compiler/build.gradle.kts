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
    compileOnly(libs.kotlin.complier.embeddable)
    implementation(libs.kotlin.poet)
    implementation(projects.core)
    implementation(libs.ksp.api)
    implementation(libs.byte.buddy)
    implementation(libs.autoService)
    ksp(libs.autoService.ksp)
    testImplementation(libs.kotlinCompileTesting.ksp)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}