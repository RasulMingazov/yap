package app.yap.admin.feature.characters.presentation

internal object TopicCountText {

    private const val HUNDRED = 100
    private const val TEN = 10
    private val FEW = 2..4
    private val TEENS = 11..14

    operator fun invoke(topicCount: Int): String = "$topicCount ${noun(topicCount)}"

    private fun noun(topicCount: Int): String = when {
        topicCount % HUNDRED in TEENS -> "тем"
        topicCount % TEN == 1 -> "тема"
        topicCount % TEN in FEW -> "темы"
        else -> "тем"
    }
}
