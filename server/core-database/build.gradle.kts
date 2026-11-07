plugins {
    alias(libs.plugins.yap.jvm.library)
    `java-test-fixtures`
}

dependencies {
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.koin.core)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.postgresql)

    testFixturesImplementation(libs.junit.jupiter)
    testFixturesImplementation(libs.testcontainers.postgresql)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}
