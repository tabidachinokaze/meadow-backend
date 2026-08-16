package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable

/** Mod 信息（API 响应） */
@Serializable
data class ModInfo(
    val id: Long,
    val modName: String,
    val modVersion: String,
    val category: String?,
)

/** 服务器 Mod 列表（9.3.1 响应） */
@Serializable
data class ServerModsResult(
    val total: Int,
    val mods: List<ModInfo>,
)
