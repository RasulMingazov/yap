package app.yap.convention

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class DetektPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("dev.detekt")

        extensions.configure<DetektExtension> {
            parallel.set(true)
            buildUponDefaultConfig.set(true)
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            source.setFrom("src")
        }

        // basePath is an absolute checkout path and a build cache key input, so every worktree
        // missed the cache. It only shortens report paths; findings keep absolute paths.
        tasks.withType<Detekt>().configureEach {
            basePath.unset()
            basePath.unsetConvention()
        }
    }
}
