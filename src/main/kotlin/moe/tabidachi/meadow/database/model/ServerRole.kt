package moe.tabidachi.meadow.database.model

/** 服务器内成员角色 */
enum class ServerRole {
    OWNER, ADMIN, MEMBER;

    val level: Int
        get() = when (this) {
            OWNER -> 3
            ADMIN -> 2
            MEMBER -> 1
        }
}
