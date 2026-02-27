import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import nl.littlerobots.vcu.plugin.versionCatalogUpdate

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.osdetector) apply false
    alias(libs.plugins.gobleyCargo) apply false
    alias(libs.plugins.gobleyUniffi) apply false
    alias(libs.plugins.atomicfu) apply false
    alias(libs.plugins.koin.compiler) apply false
    // Apply in every module
    alias(libs.plugins.versionCatalogUpdate)
    alias(libs.plugins.ktlint)
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.PREFER_STABLE)
}
