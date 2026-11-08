plugins {
    alias(libs.plugins.yap.ktor.server)
    `java-test-fixtures`
}

dependencies {
    api(libs.koin.core)
    api(project(":server:core-llm"))
    implementation(project(":server:core-database"))

    testFixturesApi(libs.stubcall)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":server:core-database")))
}
