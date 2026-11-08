plugins {
    alias(libs.plugins.yap.ktor.server)
    `java-test-fixtures`
}

dependencies {
    api(libs.koin.core)
    implementation(project(":contract-admin"))
    implementation(project(":server:core-database"))
    implementation(project(":server:core-llm"))

    testFixturesApi(libs.stubcall)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":server:core-database")))
}
