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

dependencies {
    implementation(libs.jna)
    implementation(libs.skiko.awt)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

cargo {
    builds.jvm {
        // Build Rust library only for the host platform
        embedRustLibrary = (GobleyHost.current.rustTarget == rustTarget)
    }
}