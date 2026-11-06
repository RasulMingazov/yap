package app.yap.admin.core.common

import kotlin.coroutines.cancellation.CancellationException

@Suppress("TooGenericExceptionCaught")
internal inline fun <T> runSuspendCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    Result.failure(exception)
}
