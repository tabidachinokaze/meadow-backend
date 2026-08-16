package moe.tabidachi.meadow.model

/**
 * 服务器成员模块状态码（码段 404xx，避开 ValidStatusCode / ServerStatusCode 共用的 403xx）
 */
enum class ServerMemberStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    MEMBER_NOT_FOUND(40401, "服务器成员不存在"),
    MEMBER_ALREADY_EXISTS(40402, "该用户已是服务器成员"),
    INVALID_ROLE(40403, "无效的成员角色"),
    CANNOT_MODIFY_OWNER(40404, "不能修改或移除服务器所有者"),
    CANNOT_MODIFY_SELF(40405, "不能对自己执行此操作"),
    CANNOT_MODIFY_SYSTEM_ADMIN(40406, "不能修改或移除系统管理员"),
    TRANSFER_TARGET_NOT_MEMBER(40407, "新所有者必须是服务器成员"),
    NOT_SERVER_MEMBER(40408, "你不是该服务器成员"),
}
