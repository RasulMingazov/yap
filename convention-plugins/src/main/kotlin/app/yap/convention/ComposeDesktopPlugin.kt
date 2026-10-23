package app.yap.convention

import app.yap.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposePlugin

class ComposeDesktopPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("app.yap.jvm.library")
        pluginManager.apply(libs.plugins.compose.multiplatform.get().pluginId)
        pluginManager.apply(libs.plugins.kotlin.compose.get().pluginId)

        dependencies {
            "implementation"(ComposePlugin.DesktopDependencies.currentOs)
            "implementation"(libs.compose.material3)
            "implementation"(libs.kotlinx.coroutines.swing)
        }
    }
}
