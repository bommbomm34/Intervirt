import gobley.gradle.GobleyHost
import gobley.gradle.cargo.dsl.jvm

plugins {
    kotlin("jvm")
    kotlin("plugin.atomicfu")
    alias(libs.plugins.gobleyCargo)
    alias(libs.plugins.gobleyUniffi)
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

cargo {
    builds.jvm {
        // Build Rust library only for the host platform
        embedRustLibrary = (GobleyHost.current.rustTarget == rustTarget)
    }
}