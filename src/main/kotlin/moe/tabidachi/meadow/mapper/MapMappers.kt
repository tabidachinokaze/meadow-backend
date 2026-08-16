package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.MapConfigEntity
import moe.tabidachi.meadow.database.entity.PlayerPositionEntity
import moe.tabidachi.meadow.database.model.MapConfig
import moe.tabidachi.meadow.database.model.PlayerPosition

object MapConfigMapper {
    fun toMapConfig(entity: MapConfigEntity): MapConfig = MapConfig(
        id = entity.id.value,
        serverId = entity.serverId,
        type = entity.type,
        tileUrl = entity.tileUrl,
        worldName = entity.worldName,
        centerX = entity.centerX,
        centerZ = entity.centerZ,
        zoomMin = entity.zoomMin,
        zoomMax = entity.zoomMax,
        zoomDefault = entity.zoomDefault,
        playerMarkersUrl = entity.playerMarkersUrl,
        webMapUrl = entity.webMapUrl,
        seed = entity.seed,
        updatedAt = entity.updatedAt,
    )
}

object PlayerPositionMapper {
    fun toPlayerPosition(entity: PlayerPositionEntity): PlayerPosition = PlayerPosition(
        id = entity.id.value,
        serverId = entity.serverId,
        gameUuid = entity.gameUuid,
        playerName = entity.playerName,
        x = entity.x,
        y = entity.y,
        z = entity.z,
        world = entity.world,
        updatedAt = entity.updatedAt,
    )
}
