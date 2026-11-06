plugins {
    alias(libs.plugins.yap.jvm.library)
}

dependencies {
    implementation(project(":server:core-config"))
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}
