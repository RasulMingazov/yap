plugins {
    alias(libs.plugins.yap.jvm.library)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.json)
    implementation(libs.slf4j.api)

    testFixturesApi(libs.stubcall)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
}
