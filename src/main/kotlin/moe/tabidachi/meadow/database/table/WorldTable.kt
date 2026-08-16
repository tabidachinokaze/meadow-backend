package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 存档表（规划 §9.1.8 落地） */
object WorldTable : LongIdTable("world") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val worldName = varchar("world_name", 64)
    val worldType = varchar("world_type", 20).default("survival")
    val fileSize = long("file_size").default(0)
    val isCurrent = bool("is_current").default(false)
    val lastSaved = timestamp("last_saved")
    val downloadCount = integer("download_count").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
