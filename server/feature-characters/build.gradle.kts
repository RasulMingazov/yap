plugins {
    alias(libs.plugins.yap.jvm.library)
}

dependencies {
    implementation(project(":server:core-database"))

    testImplementation(testFixtures(project(":server:core-database")))
    testImplementation(libs.kotlinx.coroutines.test)
}
