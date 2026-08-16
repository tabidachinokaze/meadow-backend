package moe.tabidachi.meadow.database.table

import moe.tabidachi.meadow.database.model.ServerRole
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 服务器成员表（规划 §9.1.2 落地）；细粒度权限位由 owner 按成员分配 */
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

    // ── 细粒度权限位（仅对 ADMIN/MEMBER 有语义；OWNER 恒为全权限）──
    val canEditServer = bool("can_edit_server").default(false)
    val canManageRcon = bool("can_manage_rcon").default(false)
    val canManageMembers = bool("can_manage_members").default(false)
    val canManageScreenshots = bool("can_manage_screenshots").default(false)
    val canManageChat = bool("can_manage_chat").default(false)
    val canManageWorlds = bool("can_manage_worlds").default(false)
    val canManageModpack = bool("can_manage_modpack").default(false)
    val canDeleteServer = bool("can_delete_server").default(false)

    val joinedAt = timestamp("joined_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(serverId, userId)
    }
}
