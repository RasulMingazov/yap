package app.yap.admin.feature.topics.domain

internal sealed interface LoadTopicsResult {

    data object Failed : LoadTopicsResult

    data class Loaded(val topics: List<Topic>) : LoadTopicsResult
}
