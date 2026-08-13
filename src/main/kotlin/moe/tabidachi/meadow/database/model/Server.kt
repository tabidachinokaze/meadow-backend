package moe.tabidachi.meadow.database.model

import moe.tabidachi.meadow.model.ModLoader
import kotlin.time.Instant

data class Server(
    val id: Long,
    val name: String?,
    val description: String?,
    val host: String,
    val port: Int,
    val modLoader: ModLoader?,
    val version: String?,
    val bannerUrl: String?,
    val tags: List<String>?,
    val ownerId: Long,
    val rconHost: String?,
    val rconPort: Int?,
    val rconPassword: String?,
    val isVerified: Boolean,
    val serverKey: String,
    val machineId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
