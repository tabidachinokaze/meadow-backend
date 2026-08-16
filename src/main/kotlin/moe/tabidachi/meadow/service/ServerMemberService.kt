package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.model.MyRoleInfo
import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.ServerMemberInfo
import moe.tabidachi.meadow.model.request.AddMemberRequest
import moe.tabidachi.meadow.model.request.TransferOwnershipRequest
import moe.tabidachi.meadow.model.request.UpdateMemberRoleRequest

interface ServerMemberService {
    /** 获取服务器成员列表（公开信息，脱敏） */
    suspend fun getMembers(serverId: Long): Response<List<ServerMemberInfo>?>

    /** 添加成员（owner / system_admin） */
    suspend fun addMember(callerId: Long, serverId: Long, request: AddMemberRequest): Response<ServerMemberInfo?>

    /** 更新成员角色（owner / system_admin） */
    suspend fun updateRole(
        callerId: Long,
        serverId: Long,
        userId: Long,
        request: UpdateMemberRoleRequest,
    ): Response<ServerMemberInfo?>

    /** 移除成员（owner / system_admin） */
    suspend fun removeMember(callerId: Long, serverId: Long, userId: Long): Response<Long?>

    /** 移交所有权（owner / system_admin） */
    suspend fun transferOwnership(
        callerId: Long,
        serverId: Long,
        request: TransferOwnershipRequest,
    ): Response<TransferResult?>

    /** 当前用户在服务器中的角色与权限位 */
    suspend fun getMyRole(callerId: Long, serverId: Long): Response<MyRoleInfo?>
}

/** 移交所有权结果 */
@kotlinx.serialization.Serializable
data class TransferResult(
    val oldOwnerId: Long,
    val newOwnerId: Long,
    val oldOwnerRole: ServerRole,
    val updatedAt: kotlin.time.Instant,
)
