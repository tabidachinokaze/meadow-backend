package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ModEntity
import moe.tabidachi.meadow.database.model.ServerMod

object ModMapper {
    fun toServerMod(entity: ModEntity): ServerMod {
        return ServerMod(
            id = entity.id.value,
            serverId = entity.serverId,
            modName = entity.modName,
            modVersion = entity.modVersion,
            modCategory = entity.modCategory,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
