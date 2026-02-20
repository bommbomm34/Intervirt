import gobley.gradle.GobleyHost
import gobley.gradle.cargo.dsl.jvm

plugins {
    kotlin("jvm")
    alias(libs.plugins.gobleyCargo)
    alias(libs.plugins.gobleyUniffi)
    alias(libs.plugins.atomicfu)
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.cryptography.core)
    implementation(libs.cryptography.provider.optimal)
}

cargo {
    builds.jvm {
        // Build Rust library only for the host platform
        embedRustLibrary = (GobleyHost.current.rustTarget == rustTarget)
    }
}