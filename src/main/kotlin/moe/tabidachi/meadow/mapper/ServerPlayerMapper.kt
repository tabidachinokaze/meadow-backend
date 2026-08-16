package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ServerPlayerEntity
import moe.tabidachi.meadow.database.model.ServerPlayer

object ServerPlayerMapper {
    fun toServerPlayer(entity: ServerPlayerEntity): ServerPlayer {
        return ServerPlayer(
            serverId = entity.serverId,
            gameUuid = entity.gameUuid,
            playerName = entity.playerName,
            firstSeen = entity.firstSeen,
            lastSeen = entity.lastSeen,
            onlineDuration = entity.onlineDuration,
            isOnline = entity.isOnline
        )
    }
}
