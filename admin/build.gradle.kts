plugins {
    alias(libs.plugins.yap.compose.desktop)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.ktorfit.get().pluginId)
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(libs.koin.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktorfit.lib.light)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel)

    testImplementation(libs.koin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.stubcall)
}

tasks.named<Test>("test") {
    outputs.dir(layout.buildDirectory.dir("screens"))
}

compose.desktop {
    application {
        mainClass = "app.yap.admin.MainKt"
    }
}
