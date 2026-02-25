plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.koin.core)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.serialization)
        }
        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(libs.docker.java.core)
            implementation(libs.docker.java.transport.httpclient5)
            implementation(libs.zip4j)
            implementation(libs.jakarta.mail)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.kotlin.logging.jvm)
            implementation(libs.slf4j.reload4j)
            implementation(libs.kommand)
            implementation(libs.sshd.core)
            implementation(libs.sshd.sftp)
        }
    }
}