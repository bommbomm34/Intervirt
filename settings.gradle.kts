rootProject.name = "Intervirt"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

sourceControl {
    gitRepository(uri("https://github.com/bommbomm34/ComposeNativeWebview.git")) {
        producesModule("io.github.bommbomm34:composenativewebview")
    }
}

include(":ui")
include(":core")
include(":secret")
includeBuild("external/webview") {
    dependencySubstitution {
        substitute(module("intervirt.webview:compose")).using(project(":webview-compose"))
    }
}
include(":logging")