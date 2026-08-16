package moe.tabidachi.meadow.model

/**
 * 玩家模块状态码（码段 406xx）
 */
enum class PlayerStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    PLAYER_NOT_FOUND(40601, "玩家不存在"),
}
