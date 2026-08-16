package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.WorldEntity
import moe.tabidachi.meadow.database.model.WorldSave

object WorldMapper {
    fun toWorldSave(entity: WorldEntity): WorldSave {
        return WorldSave(
            id = entity.id.value,
            serverId = entity.serverId,
            worldName = entity.worldName,
            worldType = entity.worldType,
            fileSize = entity.fileSize,
            isCurrent = entity.isCurrent,
            lastSaved = entity.lastSaved,
            downloadCount = entity.downloadCount,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
