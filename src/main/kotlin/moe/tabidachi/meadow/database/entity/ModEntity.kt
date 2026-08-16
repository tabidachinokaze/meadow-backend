package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.ModTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ModEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ModEntity>(ModTable)

    var serverId by ModTable.serverId
    var modName by ModTable.modName
    var modVersion by ModTable.modVersion
    var modCategory by ModTable.modCategory
    var createdAt by ModTable.createdAt
    var updatedAt by ModTable.updatedAt
}
