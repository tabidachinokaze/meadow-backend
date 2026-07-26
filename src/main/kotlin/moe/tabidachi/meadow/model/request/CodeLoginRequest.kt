package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CodeLoginRequest(
    @SerialName("email")
    val email: String,
    @SerialName("verification_code")
    val verificationCode: String,
)