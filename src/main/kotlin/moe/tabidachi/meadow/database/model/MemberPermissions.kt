package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable

/**
 * 细粒度成员权限位（数据库存储，由 owner 按成员分配）。
 * OWNER 恒为全权限（读取时强制），ADMIN/MEMBER 以本组权限为准。
 */
@Serializable
data class MemberPermissions(
    val canEditServer: Boolean = false,
    val canManageRcon: Boolean = false,
    val canManageMembers: Boolean = false,
    val canManageScreenshots: Boolean = false,
    val canManageChat: Boolean = false,
    val canManageWorlds: Boolean = false,
    val canManageModpack: Boolean = false,
    val canDeleteServer: Boolean = false,
) {
    companion object {
        /** OWNER 全权限 */
        val OWNER_ALL = MemberPermissions(
            canEditServer = true,
            canManageRcon = true,
            canManageMembers = true,
            canManageScreenshots = true,
            canManageChat = true,
            canManageWorlds = true,
            canManageModpack = true,
            canDeleteServer = true,
        )

        /** ADMIN 默认（owner 可再调整）：可编辑/管理内容，但 RCON/成员/删除需 owner 显式授予 */
        val ADMIN_DEFAULT = MemberPermissions(
            canEditServer = true,
            canManageRcon = false,
            canManageMembers = false,
            canManageScreenshots = true,
            canManageChat = true,
            canManageWorlds = true,
            canManageModpack = true,
            canDeleteServer = false,
        )

        /** MEMBER 默认：无管理权限 */
        val MEMBER_DEFAULT = MemberPermissions()
    }
}
