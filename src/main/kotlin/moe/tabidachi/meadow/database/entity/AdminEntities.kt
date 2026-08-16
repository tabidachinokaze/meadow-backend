package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.BanTable
import moe.tabidachi.meadow.database.table.ReportTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ReportEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ReportEntity>(ReportTable)

    var screenshotId by ReportTable.screenshotId
    var reporterId by ReportTable.reporterId
    var reason by ReportTable.reason
    var status by ReportTable.status
    var handlerId by ReportTable.handlerId
    var handlerNote by ReportTable.handlerNote
    var createdAt by ReportTable.createdAt
    var handledAt by ReportTable.handledAt
}

class BanEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BanEntity>(BanTable)

    var serverId by BanTable.serverId
    var playerUuid by BanTable.playerUuid
    var playerName by BanTable.playerName
    var bannedBy by BanTable.bannedBy
    var reason by BanTable.reason
    var durationHours by BanTable.durationHours
    var expiresAt by BanTable.expiresAt
    var isActive by BanTable.isActive
    var createdAt by BanTable.createdAt
}
