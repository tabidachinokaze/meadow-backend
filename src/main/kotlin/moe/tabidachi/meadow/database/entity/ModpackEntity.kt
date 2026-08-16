package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.ModpackTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ModpackEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ModpackEntity>(ModpackTable)

    var serverId by ModpackTable.serverId
    var version by ModpackTable.version
    var releaseDate by ModpackTable.releaseDate
    var downloadUrl by ModpackTable.downloadUrl
    var fileSize by ModpackTable.fileSize
    var md5Hash by ModpackTable.md5Hash
    var changelog by ModpackTable.changelog
    var downloadCount by ModpackTable.downloadCount
    var isActive by ModpackTable.isActive
    var createdAt by ModpackTable.createdAt
    var updatedAt by ModpackTable.updatedAt
}
