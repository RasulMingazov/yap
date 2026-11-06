plugins {
    alias(libs.plugins.yap.ktor.server)
    `java-test-fixtures`
}

dependencies {
    implementation(project(":contract-admin"))
    implementation(project(":server:core-database"))

    testFixturesApi(libs.stubcall)

    testImplementation(testFixtures(project(":server:core-database")))
    testImplementation(libs.kotlinx.coroutines.test)
}
