plugins {
    alias(libs.plugins.yap.jvm.library)
    `java-test-fixtures`
}

dependencies {
    implementation(project(":server:feature-characters"))

    testFixturesImplementation(project(":server:feature-characters"))
}
