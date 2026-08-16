package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.database.model.MemberPermissions
import moe.tabidachi.meadow.database.model.ServerRole

/** 添加服务器成员 */
@Serializable
data class AddMemberRequest(
    @SerialName("user_id")
    val userId: Long,
    @SerialName("role")
    val role: ServerRole,
    /** 细粒度权限（不传则按角色默认：ADMIN=ADMIN_DEFAULT，MEMBER=MEMBER_DEFAULT） */
    @SerialName("permissions")
    val permissions: MemberPermissions? = null,
)

/** 更新成员角色与权限 */
@Serializable
data class UpdateMemberRequest(
    @SerialName("role")
    val role: ServerRole? = null,
    /** 细粒度权限（不传则保持原权限；role 改为 OWNER 时强制全权限） */
    @SerialName("permissions")
    val permissions: MemberPermissions? = null,
) {
    fun isEmpty(): Boolean = role == null && permissions == null
}

/** 移交服务器所有权 */
@Serializable
data class TransferOwnershipRequest(
    @SerialName("new_owner_id")
    val newOwnerId: Long,
    @SerialName("keep_as_admin")
    val keepAsAdmin: Boolean = true,
)
