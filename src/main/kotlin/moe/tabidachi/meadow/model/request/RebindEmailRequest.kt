package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 邮箱换绑请求：新邮箱 + 发送到新邮箱的验证码 */
@Serializable
data class RebindEmailRequest(
    @SerialName("new_email")
    val newEmail: String,
    @SerialName("verification_code")
    val verificationCode: String,
)
