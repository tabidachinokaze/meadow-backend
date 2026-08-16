package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.ModInfo
import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.ServerModsResult

interface ModService {
    /** 服务器 Mod 列表（支持关键字 / 分类筛选） */
    suspend fun getServerMods(
        serverId: Long,
        keyword: String?,
        category: String?,
    ): Response<ServerModsResult?>
}
