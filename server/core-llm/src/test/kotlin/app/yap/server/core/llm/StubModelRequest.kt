package app.yap.server.core.llm

internal object StubModelRequest {

    const val MODEL = "deepseek-flash"
    const val TEXT = "Hi"

    fun stubModelRequest(
        messages: List<ModelMessage> = listOf(stubModelMessage()),
        model: String = MODEL,
    ): ModelRequest = ModelRequest(
        messages = messages,
        model = model,
    )

    fun stubModelMessage(
        role: ModelRole = ModelRole.USER,
        text: String = TEXT,
    ): ModelMessage = ModelMessage(
        role = role,
        text = text,
    )
}
