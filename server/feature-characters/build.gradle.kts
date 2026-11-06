plugins {
    alias(libs.plugins.yap.jvm.library)
    `java-test-fixtures`
}

dependencies {
    implementation(project(":server:core-database"))

    testFixturesApi(libs.stubcall)

    testImplementation(testFixtures(project(":server:core-database")))
    testImplementation(libs.kotlinx.coroutines.test)
}
