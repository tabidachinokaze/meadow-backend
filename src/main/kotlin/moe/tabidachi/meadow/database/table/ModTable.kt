package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** Mod 列表表（规划 §9.1.4 落地，数据由 Agent 状态上报全量替换） */
object ModTable : LongIdTable("mod") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val modName = varchar("mod_name", 128)
    val modVersion = varchar("mod_version", 64)
    val modCategory = varchar("mod_category", 32).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(serverId, modName)
    }
}
