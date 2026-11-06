plugins {
    alias(libs.plugins.yap.server.application)
}

dependencies {
    implementation(project(":contract-admin"))
}
