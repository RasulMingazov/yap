package app.yap.server.core.llm.deepseek

import kotlinx.serialization.Serializable

@Serializable
internal data class ChatRequestDto(
    val messages: List<ChatMessageDto>,
    val model: String,
)

@Serializable
internal data class ChatMessageDto(
    val content: String,
    val role: String,
)
