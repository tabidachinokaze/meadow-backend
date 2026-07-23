package moe.tabidachi.meadow.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val account: String,
    val password: String
)