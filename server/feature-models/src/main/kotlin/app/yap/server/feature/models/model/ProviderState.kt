package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelProviderId

data class ProviderState(
    val connected: Boolean,
    val id: ModelProviderId,
)
