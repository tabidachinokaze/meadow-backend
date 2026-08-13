package moe.tabidachi.meadow.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateEmailRequest(
    val email: String,
)
