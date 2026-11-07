package app.yap.admin.core.common

internal sealed interface LoadResult<out T> {

    data object Failed : LoadResult<Nothing>

    data class Loaded<out T>(val value: T) : LoadResult<T>
}
