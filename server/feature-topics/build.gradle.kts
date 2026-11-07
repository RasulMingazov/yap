plugins {
    alias(libs.plugins.yap.ktor.server)
    `java-test-fixtures`
}

dependencies {
    api(libs.koin.core)
    implementation(libs.exposed.java.time)
    implementation(project(":contract-admin"))
    implementation(project(":server:core-database"))
    implementation(project(":server:feature-characters"))

    testFixturesApi(libs.stubcall)
    testFixturesImplementation(testFixtures(project(":server:feature-characters")))

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":server:core-database")))
    testImplementation(testFixtures(project(":server:feature-characters")))
}
