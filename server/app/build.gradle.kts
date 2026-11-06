plugins {
    alias(libs.plugins.yap.server.application)
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(project(":server:core-config"))
    implementation(project(":server:core-database"))
    implementation(libs.ktor.server.auth)
}
