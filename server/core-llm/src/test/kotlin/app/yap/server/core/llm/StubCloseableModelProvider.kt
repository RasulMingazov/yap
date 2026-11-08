package app.yap.server.core.llm

import io.github.rasulmingazov.stubcall.StubCall0

internal class StubCloseableModelProvider(
    provider: ModelProvider = StubModelProvider(),
) : ModelProvider by provider, AutoCloseable {

    val closeCall = StubCall0.unit()

    override fun close() = closeCall.invoke()
}
