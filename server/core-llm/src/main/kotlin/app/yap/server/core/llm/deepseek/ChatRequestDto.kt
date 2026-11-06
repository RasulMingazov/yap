package app.yap.server.core.llm.deepseek

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatRequestDto(
    val messages: List<ChatMessageDto>,
    val model: String,
    @SerialName("response_format")
    val responseFormat: ResponseFormatDto?,
)

@Serializable
internal data class ChatMessageDto(
    val content: String,
    val role: String,
)

@Serializable
internal data class ResponseFormatDto(
    val type: String,
)
