import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:1.10.0")
            implementation("org.jetbrains.compose.foundation:foundation:1.10.0")
            implementation("org.jetbrains.compose.material3:material3:1.9.0")
            implementation("org.jetbrains.compose.ui:ui:1.10.0")
            implementation("org.jetbrains.compose.components:components-resources:1.10.0")
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.10.0")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation("io.github.parkwoocheol:compose-webview:1.6.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.slf4j.reload4j)
            implementation(libs.kotlin.logging.jvm)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.zip4j)
            implementation(libs.compose.tabler.icons)
            implementation(libs.commons.validator)
            implementation(libs.jediterm.core)
            implementation(libs.jediterm.ui)
            implementation(libs.webview.java)
            implementation("org.openjfx:javafx-controls:21")
            implementation("org.openjfx:javafx-web:21")
        }

    }
}


compose.desktop {
    application {
        mainClass = "io.github.bommbomm34.intervirt.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage, TargetFormat.Pkg)
            packageName = "io.github.bommbomm34.intervirt"
            packageVersion = "1.0.0"
        }
    }
}