plugins {
    alias(libs.plugins.yap.compose.desktop)
}

dependencies {
    implementation(project(":contract-admin"))

    testImplementation(libs.stubcall)
}

compose.desktop {
    application {
        mainClass = "app.yap.admin.MainKt"
    }
}
