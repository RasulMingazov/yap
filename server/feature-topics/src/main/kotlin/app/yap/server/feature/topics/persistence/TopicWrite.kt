package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.Topic

sealed interface TopicWrite {

    data object DateTaken : TopicWrite

    data object NotFound : TopicWrite

    data object StaleStatus : TopicWrite

    data class Written(val topic: Topic) : TopicWrite
}
