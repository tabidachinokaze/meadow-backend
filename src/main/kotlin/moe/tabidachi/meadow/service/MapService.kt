package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.MapConfigInfo
import moe.tabidachi.meadow.model.MapPlayersResult
import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.request.MapConfigRequest

interface MapService {
    /** 获取地图瓦片配置（9.4.1，公开读取） */
    suspend fun getConfig(serverId: Long): Response<MapConfigInfo?>

    /** 保存地图瓦片配置（管理员） */
    suspend fun saveConfig(
        adminId: Long,
        serverId: Long,
        request: MapConfigRequest,
    ): Response<MapConfigInfo?>

    /** 获取地图实时玩家位置（9.4.2，公开读取） */
    suspend fun getPlayers(serverId: Long): Response<MapPlayersResult?>
}
