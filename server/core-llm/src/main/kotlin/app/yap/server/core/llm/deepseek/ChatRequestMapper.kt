package app.yap.server.core.llm.deepseek

import app.yap.server.core.llm.ModelMessage
import app.yap.server.core.llm.ModelRequest
import app.yap.server.core.llm.ModelRole

internal fun ModelRequest.toDto(responseFormat: ResponseFormatDto?): ChatRequestDto = ChatRequestDto(
    messages = messages.map { message -> message.toDto() },
    model = model,
    responseFormat = responseFormat,
)

private fun ModelMessage.toDto(): ChatMessageDto = ChatMessageDto(
    content = text,
    role = when (role) {
        ModelRole.ASSISTANT -> "assistant"
        ModelRole.SYSTEM -> "system"
        ModelRole.USER -> "user"
    },
)
