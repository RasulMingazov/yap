plugins {
    alias(libs.plugins.yap.compose.desktop)
}

dependencies {
    implementation(project(":contract-admin"))
}

compose.desktop {
    application {
        mainClass = "app.yap.admin.MainKt"
    }
}
