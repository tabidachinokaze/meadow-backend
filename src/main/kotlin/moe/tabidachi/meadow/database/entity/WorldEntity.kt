package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.WorldTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class WorldEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<WorldEntity>(WorldTable)

    var serverId by WorldTable.serverId
    var worldName by WorldTable.worldName
    var worldType by WorldTable.worldType
    var fileSize by WorldTable.fileSize
    var isCurrent by WorldTable.isCurrent
    var lastSaved by WorldTable.lastSaved
    var downloadCount by WorldTable.downloadCount
    var createdAt by WorldTable.createdAt
    var updatedAt by WorldTable.updatedAt
}
