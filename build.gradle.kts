plugins {
    alias(libs.plugins.yap.compose.desktop) apply false
    alias(libs.plugins.yap.detekt) apply false
    alias(libs.plugins.yap.jvm.library) apply false
    alias(libs.plugins.yap.ktor.server) apply false
    alias(libs.plugins.yap.server.application) apply false
}

subprojects {
    pluginManager.apply("app.yap.detekt")
}
