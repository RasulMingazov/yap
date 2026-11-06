package app.yap.admin.feature.server.data.mapper

import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.contract.admin.ServerInfoDto

internal fun ServerInfoDto.toDomain(): ServerConnection.Connected = ServerConnection.Connected(
    environment = environment,
)
