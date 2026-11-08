import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "app.yap.convention"

dependencies {
    implementation(libs.gradle.plugin.compose)
    implementation(libs.gradle.plugin.detekt)
    implementation(libs.gradle.plugin.kotlin)
    implementation(libs.gradle.plugin.kotlin.compose)
    implementation(libs.gradle.plugin.kotlin.serialization)
    implementation(libs.gradle.plugin.ksp)
    implementation(libs.gradle.plugin.ktorfit)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

private val projectJavaVersion = JavaVersion.toVersion(libs.versions.java.get())

java {
    sourceCompatibility = projectJavaVersion
    targetCompatibility = projectJavaVersion
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(projectJavaVersion.toString()))
}

gradlePlugin {
    plugins {
        register("composeDesktop") {
            id = "app.yap.compose.desktop"
            implementationClass = "app.yap.convention.ComposeDesktopPlugin"
        }
        register("detekt") {
            id = "app.yap.detekt"
            implementationClass = "app.yap.convention.DetektPlugin"
        }
        register("jvmLibrary") {
            id = "app.yap.jvm.library"
            implementationClass = "app.yap.convention.JvmLibraryPlugin"
        }
        register("ktorServer") {
            id = "app.yap.ktor.server"
            implementationClass = "app.yap.convention.KtorServerPlugin"
        }
        register("serverApplication") {
            id = "app.yap.server.application"
            implementationClass = "app.yap.convention.ServerApplicationPlugin"
        }
    }
}
