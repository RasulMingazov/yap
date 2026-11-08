package app.yap.admin.feature.characters.presentation

internal object TopicCountText {

    operator fun invoke(topicCount: Int): String = "$topicCount ${noun(topicCount)}"

    private fun noun(topicCount: Int): String {
        val lastDigit = topicCount % 10
        return when {
            topicCount % 100 in 11..14 -> "тем"
            lastDigit == 1 -> "тема"
            lastDigit in 2..4 -> "темы"
            else -> "тем"
        }
    }
}
