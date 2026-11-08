package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.Topic

sealed interface TopicPublish {

    data object DateTaken : TopicPublish

    data object NotFound : TopicPublish

    data class Published(val topic: Topic) : TopicPublish
}
