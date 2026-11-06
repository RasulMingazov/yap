# syntax=docker/dockerfile:1

# Builds and runs the Ktor server. The image has the Gradle version of the wrapper, so the build
# downloads no distribution, and the init script resolves dependencies through mirrors that work
# from Russian hosting. Change the Gradle version here together with
# gradle/wrapper/gradle-wrapper.properties.
# The build keeps no Gradle cache between builds: a stopped build can leave a broken cache that
# fails every later build. Docker layer caching still reuses an unchanged build.
FROM gradle:9.4.1-jdk17 AS build
WORKDIR /src
COPY . .
RUN gradle --no-daemon --no-configuration-cache --console=plain \
      --init-script docker/mirrors.init.gradle.kts \
      -Dorg.gradle.jvmargs="-Xmx2g -Dfile.encoding=UTF-8" \
      :server:app:installDist

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/server/app/build/install/app /app
# A container runs as production unless the platform sets APP_ENV, so the deployed checks always run.
ENV APP_ENV=production
ENV PORT=8080
EXPOSE 8080
CMD ["/app/bin/app"]
