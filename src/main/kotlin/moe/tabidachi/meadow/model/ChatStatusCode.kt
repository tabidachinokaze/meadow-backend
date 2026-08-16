package moe.tabidachi.meadow.model

/**
 * 聊天模块状态码（码段 407xx）
 */
enum class ChatStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    MESSAGE_NOT_FOUND(40701, "消息不存在"),
}
