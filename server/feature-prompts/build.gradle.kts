plugins {
    alias(libs.plugins.yap.jvm.library)
}

dependencies {
    implementation(libs.exposed.java.time)
    implementation(project(":server:core-database"))

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":server:core-database")))
}
