package app.yap.server.admin

import app.yap.server.config.AppEnvironment

internal class GetServerEnvironmentUseCase(
    private val environment: AppEnvironment,
) {

    suspend operator fun invoke(): AppEnvironment = environment
}
