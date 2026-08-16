package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 密码重置请求：邮箱 + 验证码 + 新密码 */
@Serializable
data class ResetPasswordRequest(
    @SerialName("email")
    val email: String,
    @SerialName("verification_code")
    val verificationCode: String,
    @SerialName("new_password")
    val newPassword: String,
)
