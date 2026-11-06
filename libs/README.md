# libs

A file-based Maven repository committed with the project. It holds only the published
`io.github.rasulmingazov:stubcall` artifacts, version `0.1.0`, copied from `RasulMingazov/yap-app`.
`settings.gradle.kts` declares it; `gradle/libs.versions.toml` pins the version.

- `stubcall` is the multiplatform root module; `stubcall-jvm` is the JVM variant the admin app and
  the server use. The mobile app adds its iOS variants when it needs them.
- Each file has its published `.md5`, `.sha1`, `.sha256`, and `.sha512` checksums.
