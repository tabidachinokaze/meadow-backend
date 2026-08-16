package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.database.model.ServerRole

/** 添加服务器成员 */
@Serializable
data class AddMemberRequest(
    @SerialName("user_id")
    val userId: Long,
    @SerialName("role")
    val role: ServerRole,
)

/** 更新成员角色 */
@Serializable
data class UpdateMemberRoleRequest(
    @SerialName("role")
    val role: ServerRole,
)

/** 移交服务器所有权 */
@Serializable
data class TransferOwnershipRequest(
    @SerialName("new_owner_id")
    val newOwnerId: Long,
    @SerialName("keep_as_admin")
    val keepAsAdmin: Boolean = true,
)
