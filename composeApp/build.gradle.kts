import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val javaFXVersion = "21"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.osdetector)
}

kotlin {
    jvm()

    sourceSets {
        // https://stackoverflow.com/questions/73187027/use-javafx-in-kotlin-multiplatform
        val javaFXSuffix = when (osdetector.classifier){
            "linux-x86_64" -> "linux"
            "linux-aarch_64" -> "linux-aarch64"
            "windows-x86_64" -> "win"
            "osx-x86_64" -> "mac"
            "osx-aarch_64" -> "mac-aarch64"
            else -> throw IllegalStateException("Unknown OS: ${osdetector.classifier}")
        }

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
            implementation(libs.sshd.core)
            implementation(libs.sshd.sftp)
            implementation(libs.kommand)
            implementation(libs.compose.table)
            implementation(kotlin("reflect"))
            // JavaFX
            implementation("org.openjfx:javafx-base:$javaFXVersion:${javaFXSuffix}")
            implementation("org.openjfx:javafx-graphics:$javaFXVersion:${javaFXSuffix}")
            implementation("org.openjfx:javafx-controls:$javaFXVersion:${javaFXSuffix}")
            implementation("org.openjfx:javafx-web:$javaFXVersion:${javaFXSuffix}")
            implementation("org.openjfx:javafx-swing:$javaFXVersion:${javaFXSuffix}")
            implementation("org.openjfx:javafx-media:$javaFXVersion:${javaFXSuffix}")
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