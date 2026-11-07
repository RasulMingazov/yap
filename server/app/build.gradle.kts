plugins {
    alias(libs.plugins.yap.server.application)
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(project(":server:core-database"))
    implementation(project(":server:feature-characters"))
    implementation(project(":server:feature-levels"))
    implementation(project(":server:feature-topics"))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.status.pages)

    testImplementation(testFixtures(project(":server:core-database")))
    testImplementation(testFixtures(project(":server:feature-characters")))
    testImplementation(testFixtures(project(":server:feature-levels")))
    testImplementation(testFixtures(project(":server:feature-topics")))
    testImplementation(libs.koin.test)

    constraints {
        implementation(libs.ktor.server.di) {
            because("koin-ktor brings a ktor-server-di older than the Ktor version of the server")
        }
    }
}
