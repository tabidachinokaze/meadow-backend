package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserInfoRequest(
    @SerialName("username")
    val username: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("banner_url")
    val bannerUrl: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("website")
    val website: String? = null,
    @SerialName("location")
    val location: String? = null,
) {
    fun isEmpty() = listOf(
        username,
        email,
        phone,
        avatarUrl,
        bannerUrl,
        bio,
        website,
        location,
    ).all { it == null }
}
