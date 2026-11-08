package app.yap.admin.core.presentation

internal data class Status(
    val text: String,
    val tone: Tone,
) {

    enum class Tone {
        ERROR,
        MUTED,
        SUCCESS,
    }
}
