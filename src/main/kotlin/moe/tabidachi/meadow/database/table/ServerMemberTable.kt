package moe.tabidachi.meadow.database.table

import moe.tabidachi.meadow.database.model.ServerRole
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 服务器成员表（规划 §9.1.2 落地） */
object ServerMemberTable : LongIdTable("server_member") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val userId = long("user_id").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val role = enumeration<ServerRole>("role")
    val joinedAt = timestamp("joined_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(serverId, userId)
    }
}
