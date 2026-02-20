plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"

dependencies {
    implementation(libs.sshd.core)
    implementation(libs.sshd.sftp)
    implementation(libs.kommand)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.core)
    implementation(libs.slf4j.reload4j)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.zip4j)
    implementation(libs.jediterm.core)
    implementation(libs.jediterm.ui)
    implementation(libs.jakarta.mail)
    implementation(libs.docker.java.core)
    implementation(libs.docker.java.transport.httpclient5)
    implementation(libs.ksafe)
    // Test
    testImplementation(libs.koin.test)
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}