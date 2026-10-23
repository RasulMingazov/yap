// Puts Maven mirrors that work from Russian hosting before the repositories of every settings
// file. Tencent Cloud `maven-public` proxies Maven Central and Google Maven; `gradle-plugins`
// proxies the Gradle Plugin Portal. The default repositories stay after them as the fallback.
// Only the Dockerfile uses this script: local builds and CI keep the repositories from the
// settings files.
val libraryMirror = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public"
val pluginMirror = "https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugins"

beforeSettings {
    pluginManagement.repositories {
        maven { url = uri(libraryMirror) }
        maven { url = uri(pluginMirror) }
    }
    dependencyResolutionManagement.repositories { maven { url = uri(libraryMirror) } }
}
