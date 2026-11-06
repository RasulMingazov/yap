package app.yap.admin.feature.characters.domain

internal sealed interface LoadResult<out T> {

    data object Failed : LoadResult<Nothing>

    data class Loaded<out T>(val value: T) : LoadResult<T>
}
