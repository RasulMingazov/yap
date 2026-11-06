package app.yap.server.core.llm

sealed interface ModelReply<out T> {

    data class Answered<out T>(
        val value: T,
    ) : ModelReply<T>

    data object Failed : ModelReply<Nothing>
}
