plugins {
    alias(libs.plugins.yap.server.application)
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(project(":server:core-database"))
    implementation(project(":server:feature-characters"))
    implementation(libs.ktor.server.auth)

    testImplementation(testFixtures(project(":server:feature-characters")))
}
