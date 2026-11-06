package app.yap.admin.core.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal fun <T, R> StateFlow<T>.mapState(scope: CoroutineScope, transform: (T) -> R): StateFlow<R> = this
    .map(transform)
    .stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = transform(value),
    )
