package app.yap.admin.feature.models.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.StubModels
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetModelsUseCase(
    models: LoadResult<Models> = LoadResult.Loaded(StubModels.stubModels()),
) : GetModelsUseCase {

    val invokeCall = StubCall0.returns<LoadResult<Models>>(models)

    override suspend fun invoke(): LoadResult<Models> = invokeCall.invoke()
}
