import nl.littlerobots.vcu.plugin.resolver.VersionSelectors

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.aboutLibraries) apply false
    id("org.openjfx.javafxplugin") version "0.1.0"
    alias(libs.plugins.versionCatalogUpdate)
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}