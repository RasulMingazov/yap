plugins {
    alias(libs.plugins.yap.jvm.library)
    `java-test-fixtures`
}

dependencies {
    implementation(libs.exposed.java.time)
    implementation(project(":server:core-database"))

    testFixturesApi(libs.stubcall)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":server:core-database")))
}
