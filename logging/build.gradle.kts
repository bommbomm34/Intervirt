plugins {
    kotlin("jvm")
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"

dependencies {
    implementation(libs.kotlinx.datetime)
}

kotlin {
    jvmToolchain(21)
}