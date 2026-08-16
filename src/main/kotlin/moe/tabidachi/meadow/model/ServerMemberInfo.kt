package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.database.model.MemberPermissions
import moe.tabidachi.meadow.database.model.ServerMember
import moe.tabidachi.meadow.database.model.ServerRole

/** 服务器成员信息（API 响应，用户信息已脱敏） */
@Serializable
data class ServerMemberInfo(
    val userId: Long,
    val username: String,
    val gameId: String?,
    val avatarUrl: String?,
    val role: ServerRole,
    /** 细粒度权限位（owner 可分配/查看） */
    val permissions: MyRoleInfo.Permissions = MyRoleInfo.Permissions(),
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
        val canManageRcon: Boolean = false,
        val canManageMembers: Boolean = false,
        val canManageScreenshots: Boolean = false,
        val canManageChat: Boolean = false,
        val canManageWorlds: Boolean = false,
        val canManageModpack: Boolean = false,
        val canDeleteServer: Boolean = false,
    )

    companion object {
        /**
         * 从成员记录构建权限。
         * 注意：系统管理员不再自动拥有全部权限——需被加入服务器并由 owner 分配（走正常权限流程）。
         */
        fun of(member: ServerMember?): MyRoleInfo {
            if (member == null) return MyRoleInfo(role = null, permissions = Permissions())
            return MyRoleInfo(
                role = member.role,
                permissions = fromPermissions(member.permissions),
            )
        }

        /** MemberPermissions（数据库）→ Permissions（API） */
        fun fromPermissions(p: MemberPermissions): Permissions = Permissions(
            canEditServer = p.canEditServer,
            canManageRcon = p.canManageRcon,
            canManageMembers = p.canManageMembers,
            canManageScreenshots = p.canManageScreenshots,
            canManageChat = p.canManageChat,
            canManageWorlds = p.canManageWorlds,
            canManageModpack = p.canManageModpack,
            canDeleteServer = p.canDeleteServer,
        )

        /** 空权限（非成员） */
        fun empty(): Permissions = Permissions()
    }
}
