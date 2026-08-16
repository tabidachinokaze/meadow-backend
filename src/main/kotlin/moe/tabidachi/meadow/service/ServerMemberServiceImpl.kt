package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.MemberPermissions
import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.AddMemberRequest
import moe.tabidachi.meadow.model.request.TransferOwnershipRequest
import moe.tabidachi.meadow.model.request.UpdateMemberRequest
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import kotlin.time.Clock

class ServerMemberServiceImpl(
    private val serverMemberRepository: ServerMemberRepository,
    private val serverRepository: ServerRepository,
    private val userRepository: UserRepository,
    private val database: org.jetbrains.exposed.v1.jdbc.Database,
) : ServerMemberService {

    override suspend fun getMembers(serverId: Long): Response<List<ServerMemberInfo>?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val members = serverMemberRepository.getByServer(serverId)
        val infos = members.mapNotNull { member ->
            userRepository.getUserInfo(member.userId)?.let { user ->
                ServerMemberInfo(
                    userId = user.uid,
                    username = user.username,
                    gameId = user.gameId,
                    avatarUrl = user.avatarUrl,
                    role = member.role,
                    permissions = MyRoleInfo.fromPermissions(member.permissions),
                    joinedAt = member.joinedAt,
                )
            }
        }
        return CommonStatusCode.SUCCESS.withData(infos)
    }

    override suspend fun addMember(
        callerId: Long,
        serverId: Long,
        request: AddMemberRequest,
    ): Response<ServerMemberInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!canManageMembers(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (request.role == ServerRole.OWNER) {
            return ServerMemberStatusCode.INVALID_ROLE.emptyData()
        }
        if (userRepository.getByUid(request.userId) == null) {
            return UserStatusCode.USER_NOT_FOUND.emptyData()
        }
        if (serverMemberRepository.getByServerAndUser(serverId, request.userId) != null) {
            return ServerMemberStatusCode.MEMBER_ALREADY_EXISTS.emptyData()
        }
        // 权限分配仅限 ADMIN：MEMBER 忽略请求权限，强制为空权限（与 updateMember 一致）
        val permissions = when (request.role) {
            ServerRole.ADMIN -> request.permissions ?: MemberPermissions.ADMIN_DEFAULT
            else -> MemberPermissions.MEMBER_DEFAULT
        }
        val member = serverMemberRepository.add(serverId, request.userId, request.role, permissions)
        val user = userRepository.getUserInfo(member.userId) ?: return CommonStatusCode.FAILURE.emptyData()
        return CommonStatusCode.SUCCESS.withData(
            ServerMemberInfo(
                userId = user.uid,
                username = user.username,
                gameId = user.gameId,
                avatarUrl = user.avatarUrl,
                role = member.role,
                permissions = MyRoleInfo.fromPermissions(member.permissions),
                joinedAt = member.joinedAt,
            )
        )
    }

    override suspend fun updateMember(
        callerId: Long,
        serverId: Long,
        userId: Long,
        request: UpdateMemberRequest,
    ): Response<ServerMemberInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!canManageMembers(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (request.isEmpty()) {
            return ServerMemberStatusCode.INVALID_ROLE.emptyData()
        }
        if (callerId == userId) {
            return ServerMemberStatusCode.CANNOT_MODIFY_SELF.emptyData()
        }
        val target = serverMemberRepository.getByServerAndUser(serverId, userId)
            ?: return ServerMemberStatusCode.MEMBER_NOT_FOUND.emptyData()
        if (target.role == ServerRole.OWNER) {
            return ServerMemberStatusCode.CANNOT_MODIFY_OWNER.emptyData()
        }

        // 目标最终角色（可能随本次请求变更）
        val effectiveRole = request.role ?: target.role

        // 角色变更
        if (request.role != null) {
            if (request.role == ServerRole.OWNER) {
                return ServerMemberStatusCode.INVALID_ROLE.emptyData()
            }
            if (!serverMemberRepository.updateRole(serverId, userId, request.role)) {
                return CommonStatusCode.FAILURE.emptyData()
            }
        }
        // 权限分配仅限 ADMIN：MEMBER 恒为空权限（role 改为 MEMBER 或本就是 MEMBER 时强制重置）
        if (effectiveRole == ServerRole.ADMIN) {
            if (request.permissions != null) {
                if (!serverMemberRepository.updatePermissions(serverId, userId, request.permissions)) {
                    return CommonStatusCode.FAILURE.emptyData()
                }
            }
        } else {
            // MEMBER（或未来其他非 ADMIN 角色）：不允许携带权限位，重置为空权限
            if (!serverMemberRepository.updatePermissions(serverId, userId, MemberPermissions.MEMBER_DEFAULT)) {
                return CommonStatusCode.FAILURE.emptyData()
            }
        }
        val updated = serverMemberRepository.getByServerAndUser(serverId, userId)
            ?: return CommonStatusCode.FAILURE.emptyData()
        val user = userRepository.getUserInfo(userId) ?: return CommonStatusCode.FAILURE.emptyData()
        return CommonStatusCode.SUCCESS.withData(
            ServerMemberInfo(
                userId = user.uid,
                username = user.username,
                gameId = user.gameId,
                avatarUrl = user.avatarUrl,
                role = updated.role,
                permissions = MyRoleInfo.fromPermissions(updated.permissions),
                joinedAt = updated.joinedAt,
            )
        )
    }

    override suspend fun removeMember(callerId: Long, serverId: Long, userId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!canManageMembers(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (callerId == userId) {
            return ServerMemberStatusCode.CANNOT_MODIFY_SELF.emptyData()
        }
        val target = serverMemberRepository.getByServerAndUser(serverId, userId)
            ?: return ServerMemberStatusCode.MEMBER_NOT_FOUND.emptyData()
        if (target.role == ServerRole.OWNER) {
            return ServerMemberStatusCode.CANNOT_MODIFY_OWNER.emptyData()
        }
        if (!serverMemberRepository.remove(serverId, userId)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(userId)
    }

    override suspend fun transferOwnership(
        callerId: Long,
        serverId: Long,
        request: TransferOwnershipRequest,
    ): Response<TransferResult?> {
        val server = serverRepository.getById(serverId)
            ?: return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        if (!canManageMembers(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (server.ownerId == request.newOwnerId) {
            return ServerMemberStatusCode.MEMBER_ALREADY_EXISTS.emptyData()
        }
        if (userRepository.getByUid(request.newOwnerId) == null) {
            return UserStatusCode.USER_NOT_FOUND.emptyData()
        }
        val oldOwnerRole = serverMemberRepository.getByServerAndUser(serverId, server.ownerId)?.role
            ?: ServerRole.OWNER
        // 服务级事务：加成员→降旧主→改权限→升新主→改 owner_id 原子完成（中途失败整体回滚）
        val ok = database.withTransaction {
            // 新所有者若还不是成员则自动添加为 member，再升级为 owner
            if (serverMemberRepository.getByServerAndUser(serverId, request.newOwnerId) == null) {
                serverMemberRepository.add(serverId, request.newOwnerId, ServerRole.MEMBER, MemberPermissions.MEMBER_DEFAULT)
            }
            // 旧 owner 降级：keep_as_admin 时给 ADMIN 默认权限
            if (!serverMemberRepository.updateRole(
                    serverId, server.ownerId,
                    if (request.keepAsAdmin) ServerRole.ADMIN else ServerRole.MEMBER
                )) {
                return@withTransaction false
            }
            if (request.keepAsAdmin) {
                serverMemberRepository.updatePermissions(serverId, server.ownerId, MemberPermissions.ADMIN_DEFAULT)
            }
            if (!serverMemberRepository.updateRole(serverId, request.newOwnerId, ServerRole.OWNER)) {
                return@withTransaction false
            }
            if (!serverRepository.update(serverId = serverId, ownerId = request.newOwnerId)) {
                return@withTransaction false
            }
            true
        }
        if (!ok) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(
            TransferResult(
                oldOwnerId = server.ownerId,
                newOwnerId = request.newOwnerId,
                oldOwnerRole = oldOwnerRole,
                updatedAt = Clock.System.now(),
            )
        )
    }

    override suspend fun getMyRole(callerId: Long, serverId: Long): Response<MyRoleInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        // 非成员 → 空权限；系统管理员同样需被分配权限（走正常流程）
        val member = serverMemberRepository.getByServerAndUser(serverId, callerId)
        return CommonStatusCode.SUCCESS.withData(MyRoleInfo.of(member))
    }

    // ── 权限辅助 ──

    /** 是否可管理成员：owner（恒有）或具备 canManageMembers 权限位的成员 */
    private suspend fun canManageMembers(callerId: Long, serverId: Long): Boolean {
        val member = serverMemberRepository.getByServerAndUser(serverId, callerId) ?: return false
        return member.role == ServerRole.OWNER || member.permissions.canManageMembers
    }
}
