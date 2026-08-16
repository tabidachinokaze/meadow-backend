package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.ServerPlayerTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ServerPlayerEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ServerPlayerEntity>(ServerPlayerTable)

    var serverId by ServerPlayerTable.serverId
    var gameUuid by ServerPlayerTable.gameUuid
    var playerName by ServerPlayerTable.playerName
    var firstSeen by ServerPlayerTable.firstSeen
    var lastSeen by ServerPlayerTable.lastSeen
    var onlineDuration by ServerPlayerTable.onlineDuration
    var isOnline by ServerPlayerTable.isOnline
}
