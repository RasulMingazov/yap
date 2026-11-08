plugins {
    alias(libs.plugins.yap.ktor.server)
    `java-test-fixtures`
}

dependencies {
    implementation(project(":server:feature-levels"))
    implementation(project(":server:feature-topics"))

    testFixturesImplementation(project(":server:feature-levels"))
    testFixturesImplementation(project(":server:feature-topics"))
    testFixturesImplementation(testFixtures(project(":server:feature-topics")))

    testImplementation(testFixtures(project(":server:feature-topics")))
}
