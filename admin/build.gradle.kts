plugins {
    alias(libs.plugins.yap.compose.desktop)
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.stubcall)
}

compose.desktop {
    application {
        mainClass = "app.yap.admin.MainKt"
    }
}
