package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.ScreenshotTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ScreenshotEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ScreenshotEntity>(ScreenshotTable)

    var serverId by ScreenshotTable.serverId
    var uploaderId by ScreenshotTable.uploaderId
    var uploaderName by ScreenshotTable.uploaderName
    var imageUrl by ScreenshotTable.imageUrl
    var description by ScreenshotTable.description
    var coordinates by ScreenshotTable.coordinates
    var status by ScreenshotTable.status
    var reportCount by ScreenshotTable.reportCount
    var downloadCount by ScreenshotTable.downloadCount
    var createdAt by ScreenshotTable.createdAt
}
