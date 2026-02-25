import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.coil.compose)
            implementation(libs.bossterm.compose)
            implementation(libs.compose.table)
            implementation(libs.coil.network.ktor3)
            implementation(libs.material.kolor)
            implementation(libs.compose.colorpicker)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.slf4j.reload4j)
            implementation(libs.kotlin.logging.jvm)
            implementation(libs.commons.validator)
            implementation(libs.bossterm.core)
            implementation(projects.core)
        }
    }
}


compose.desktop {
    application {
        mainClass = "io.github.bommbomm34.intervirt.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
            packageName = "io.github.bommbomm34.intervirt"
            packageVersion = "0.0.1"

            windows {
                iconFile.set(project.file("metadata/icon.ico"))

                // Platform-specific
                perUserInstall = true
                dirChooser = true
            }

            linux {
                iconFile.set(project.file("metadata/icon.png"))

                // Platform-specific
                packageName = "intervirt"
                debMaintainer = "bommbomm34@perhof.org"
            }
        }
    }
}