package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.StubLevel
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetLevelsUseCase(
    levels: LoadResult<List<Level>> = LoadResult.Loaded(StubLevel.stubLevels()),
) : GetLevelsUseCase {

    val invokeCall = StubCall0.returns<LoadResult<List<Level>>>(levels)

    override suspend fun invoke(): LoadResult<List<Level>> = invokeCall.invoke()
}
