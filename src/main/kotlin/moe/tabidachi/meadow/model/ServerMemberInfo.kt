package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.database.model.ServerRole

/** 服务器成员信息（API 响应，用户信息已脱敏） */
@Serializable
data class ServerMemberInfo(
    val userId: Long,
    val username: String,
    val gameId: String?,
    val avatarUrl: String?,
    val role: ServerRole,
    val joinedAt: kotlin.time.Instant?,
)

/** 当前用户在服务器中的角色与权限位（my-role） */
@Serializable
data class MyRoleInfo(
    val role: ServerRole?,
    val permissions: Permissions,
) {
    @Serializable
    data class Permissions(
        val canEditServer: Boolean = false,
        val canManageMembers: Boolean = false,
        val canManageScreenshots: Boolean = false,
        val canManageChat: Boolean = false,
        val canManageWorlds: Boolean = false,
        val canManageModpack: Boolean = false,
        val canDeleteServer: Boolean = false,
    )

    companion object {
        fun of(role: ServerRole?, isSystemAdmin: Boolean): MyRoleInfo {
            val manage = role == ServerRole.OWNER || role == ServerRole.ADMIN || isSystemAdmin
            return MyRoleInfo(
                role = role,
                permissions = Permissions(
                    canEditServer = manage,
                    canManageMembers = role == ServerRole.OWNER || isSystemAdmin,
                    canManageScreenshots = manage,
                    canManageChat = manage,
                    canManageWorlds = manage,
                    canManageModpack = manage,
                    canDeleteServer = role == ServerRole.OWNER || isSystemAdmin,
                ),
            )
        }
    }
}
