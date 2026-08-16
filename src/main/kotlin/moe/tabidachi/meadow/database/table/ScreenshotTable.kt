package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 截图表（规划 §9.1.6 落地） */
object ScreenshotTable : LongIdTable("screenshot") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val uploaderId = long("uploader_id").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val uploaderName = varchar("uploader_name", 64)
    val imageUrl = text("image_url")
    val description = text("description").nullable()
    val coordinates = varchar("coordinates", 64).nullable()
    val status = varchar("status", 20).default("active")
    val reportCount = integer("report_count").default(0)
    val downloadCount = integer("download_count").default(0)
    val createdAt = timestamp("created_at")

    init {
        index(false, serverId)
        index(false, uploaderId)
        index(false, status)
    }
}
