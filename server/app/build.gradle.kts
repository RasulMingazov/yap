plugins {
    alias(libs.plugins.yap.server.application)
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(project(":server:core-database"))
    implementation(project(":server:feature-characters"))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.status.pages)

    testImplementation(testFixtures(project(":server:core-database")))
    testImplementation(testFixtures(project(":server:feature-characters")))
    testImplementation(libs.koin.test)
}
