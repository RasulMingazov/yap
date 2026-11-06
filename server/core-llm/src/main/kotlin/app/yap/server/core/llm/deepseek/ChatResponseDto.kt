package app.yap.server.core.llm.deepseek

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatResponseDto(
    val choices: List<ChatChoiceDto>,
)

@Serializable
internal data class ChatChoiceDto(
    @SerialName("finish_reason")
    val finishReason: String?,
    val message: ChatReplyDto,
)

@Serializable
internal data class ChatReplyDto(
    val content: String?,
)
