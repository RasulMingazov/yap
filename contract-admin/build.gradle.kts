plugins {
    alias(libs.plugins.yap.jvm.library)
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    api(libs.kotlinx.serialization.json)
}
