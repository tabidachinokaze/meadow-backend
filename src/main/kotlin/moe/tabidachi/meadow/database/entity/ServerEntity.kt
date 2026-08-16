package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.ServerTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ServerEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ServerEntity>(ServerTable)

    var name by ServerTable.name
    var description by ServerTable.description
    var host by ServerTable.host
    var port by ServerTable.port
    var modLoader by ServerTable.modLoader
    var version by ServerTable.version
    var bannerUrl by ServerTable.bannerUrl
    var tags by ServerTable.tags
    var ownerId by ServerTable.ownerId
    var rconHost by ServerTable.rconHost
    var rconPort by ServerTable.rconPort
    var rconPassword by ServerTable.rconPassword
    var isVerified by ServerTable.isVerified
    var serverKey by ServerTable.serverKey
    var machineId by ServerTable.machineId
    var onlinePlayers by ServerTable.onlinePlayers
    var maxPlayers by ServerTable.maxPlayers
    var uptimeSeconds by ServerTable.uptimeSeconds
    var lastStatusAt by ServerTable.lastStatusAt
    var createdAt by ServerTable.createdAt
    var updatedAt by ServerTable.updatedAt
}
