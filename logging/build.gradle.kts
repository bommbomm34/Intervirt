@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.bommbomm34.intervirt"
version = "0.0.1"


kotlin {
    jvm()
    js {
        defaultBrowser()
    }
    wasmJs {
        defaultBrowser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    linuxX64()
    linuxArm64()
    mingwX64()
}

fun KotlinJsTargetDsl.defaultBrowser(){
    browser {
        testTask {
            useKarma {
                useChromiumHeadless()
            }
        }
    }
}