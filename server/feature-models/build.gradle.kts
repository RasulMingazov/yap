plugins {
    alias(libs.plugins.yap.jvm.library)
    `java-test-fixtures`
}

dependencies {
    api(project(":server:core-llm"))
    implementation(project(":server:core-database"))

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":server:core-database")))
}
