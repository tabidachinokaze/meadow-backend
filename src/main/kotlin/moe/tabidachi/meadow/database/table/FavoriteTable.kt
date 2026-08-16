package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 收藏表（规划 §9.1.1 落地） */
object FavoriteTable : LongIdTable("favorite") {
    val userId = long("user_id").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(userId, serverId)
    }
}
