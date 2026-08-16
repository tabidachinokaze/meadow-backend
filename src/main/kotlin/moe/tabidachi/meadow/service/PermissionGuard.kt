package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.repository.ServerMemberRepository

/**
 * 细粒度权限位校验（v2 权限模型统一入口）。
 *
 * 规则：OWNER 恒为全权限；其他角色按成员记录中的权限位判断；
 * 非成员一律无权限（系统管理员同样需被分配权限，走正常流程）。
 * 各服务的管理操作应统一通过 [hasPermission] / [requirePermission] 校验，
 * 禁止再按 role 判断（否则权限位形同虚设）。
 */
interface PermissionGuard {
    /** 当前用户是否拥有该权限位（非成员/权限位关闭 → false） */
    suspend fun hasPermission(serverId: Long, userId: Long, bit: PermissionBit): Boolean

    /** 校验并返回是否通过（供服务层短路用） */
    suspend fun requirePermission(serverId: Long, userId: Long, bit: PermissionBit): Boolean
}

/** 权限位语义（与 MemberPermissions 字段一一对应） */
enum class PermissionBit {
    CAN_EDIT_SERVER,
    CAN_MANAGE_RCON,
    CAN_MANAGE_MEMBERS,
    CAN_MANAGE_SCREENSHOTS,
    CAN_MANAGE_CHAT,
    CAN_MANAGE_WORLDS,
    CAN_MANAGE_MODPACK,
    CAN_DELETE_SERVER,
}

class PermissionGuardImpl(
    private val serverMemberRepository: ServerMemberRepository,
) : PermissionGuard {
    override suspend fun hasPermission(serverId: Long, userId: Long, bit: PermissionBit): Boolean {
        val member = serverMemberRepository.getByServerAndUser(serverId, userId) ?: return false
        // OWNER 恒为全权限
        if (member.role == ServerRole.OWNER) return true
        val p = member.permissions
        return when (bit) {
            PermissionBit.CAN_EDIT_SERVER -> p.canEditServer
            PermissionBit.CAN_MANAGE_RCON -> p.canManageRcon
            PermissionBit.CAN_MANAGE_MEMBERS -> p.canManageMembers
            PermissionBit.CAN_MANAGE_SCREENSHOTS -> p.canManageScreenshots
            PermissionBit.CAN_MANAGE_CHAT -> p.canManageChat
            PermissionBit.CAN_MANAGE_WORLDS -> p.canManageWorlds
            PermissionBit.CAN_MANAGE_MODPACK -> p.canManageModpack
            PermissionBit.CAN_DELETE_SERVER -> p.canDeleteServer
        }
    }

    override suspend fun requirePermission(serverId: Long, userId: Long, bit: PermissionBit): Boolean =
        hasPermission(serverId, userId, bit)
}
