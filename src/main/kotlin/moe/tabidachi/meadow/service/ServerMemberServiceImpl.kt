package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.model.SystemRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.AddMemberRequest
import moe.tabidachi.meadow.model.request.TransferOwnershipRequest
import moe.tabidachi.meadow.model.request.UpdateMemberRoleRequest
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import kotlin.time.Clock

class ServerMemberServiceImpl(
    private val serverMemberRepository: ServerMemberRepository,
    private val serverRepository: ServerRepository,
    private val userRepository: UserRepository,
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
        if (!isOwnerOrSystemAdmin(callerId, serverId)) {
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
        val member = serverMemberRepository.add(serverId, request.userId, request.role)
        val user = userRepository.getUserInfo(member.userId) ?: return CommonStatusCode.FAILURE.emptyData()
        return CommonStatusCode.SUCCESS.withData(
            ServerMemberInfo(
                userId = user.uid,
                username = user.username,
                gameId = user.gameId,
                avatarUrl = user.avatarUrl,
                role = member.role,
                joinedAt = member.joinedAt,
            )
        )
    }

    override suspend fun updateRole(
        callerId: Long,
        serverId: Long,
        userId: Long,
        request: UpdateMemberRoleRequest,
    ): Response<ServerMemberInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrSystemAdmin(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (request.role == ServerRole.OWNER) {
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
        if (isSystemAdmin(userId)) {
            return ServerMemberStatusCode.CANNOT_MODIFY_SYSTEM_ADMIN.emptyData()
        }
        if (!serverMemberRepository.updateRole(serverId, userId, request.role)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        val user = userRepository.getUserInfo(userId) ?: return CommonStatusCode.FAILURE.emptyData()
        return CommonStatusCode.SUCCESS.withData(
            ServerMemberInfo(
                userId = user.uid,
                username = user.username,
                gameId = user.gameId,
                avatarUrl = user.avatarUrl,
                role = request.role,
                joinedAt = target.joinedAt,
            )
        )
    }

    override suspend fun removeMember(callerId: Long, serverId: Long, userId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrSystemAdmin(callerId, serverId)) {
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
        if (isSystemAdmin(userId)) {
            return ServerMemberStatusCode.CANNOT_MODIFY_SYSTEM_ADMIN.emptyData()
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
        if (!isOwnerOrSystemAdmin(callerId, serverId)) {
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
        // 新所有者若还不是成员则自动添加为 member，再升级为 owner
        if (serverMemberRepository.getByServerAndUser(serverId, request.newOwnerId) == null) {
            serverMemberRepository.add(serverId, request.newOwnerId, ServerRole.MEMBER)
        }
        if (!serverMemberRepository.updateRole(serverId, server.ownerId, if (request.keepAsAdmin) ServerRole.ADMIN else ServerRole.MEMBER)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        if (!serverMemberRepository.updateRole(serverId, request.newOwnerId, ServerRole.OWNER)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        if (!serverRepository.update(serverId = serverId, ownerId = request.newOwnerId)) {
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
        val role = serverMemberRepository.getByServerAndUser(serverId, callerId)?.role
        val isSystemAdmin = isSystemAdmin(callerId)
        return CommonStatusCode.SUCCESS.withData(MyRoleInfo.of(role, isSystemAdmin))
    }

    // ── 权限辅助 ──

    /** 是否为系统管理员（SystemRole.ADMIN，映射原规划的 system_admin） */
    private suspend fun isSystemAdmin(userId: Long): Boolean =
        userRepository.getByUid(userId)?.role == SystemRole.ADMIN

    /** 调用者是否为该服务器 owner 或系统管理员（管理成员所需的最低权限） */
    private suspend fun isOwnerOrSystemAdmin(callerId: Long, serverId: Long): Boolean {
        if (isSystemAdmin(callerId)) return true
        return serverMemberRepository.getByServerAndUser(serverId, callerId)?.role == ServerRole.OWNER
    }
}
