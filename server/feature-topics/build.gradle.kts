plugins {
    alias(libs.plugins.yap.jvm.library)
    `java-test-fixtures`
}

dependencies {
    implementation(project(":server:feature-characters"))

    testFixturesImplementation(testFixtures(project(":server:feature-characters")))
}
