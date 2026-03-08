@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"


kotlin {
    jvm()
    js()
    wasmJs()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
    }

    linuxX64()
    linuxArm64()
    mingwX64()
}