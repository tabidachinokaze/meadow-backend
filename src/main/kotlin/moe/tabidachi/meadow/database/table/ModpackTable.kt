package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 整合包表（规划 §9.1.9 落地） */
object ModpackTable : LongIdTable("modpack") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val version = varchar("version", 32)
    val releaseDate = varchar("release_date", 16)
    val downloadUrl = text("download_url")
    val fileSize = long("file_size").default(0)
    val md5Hash = varchar("md5_hash", 64)
    val changelog = text("changelog").nullable()
    val downloadCount = integer("download_count").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
