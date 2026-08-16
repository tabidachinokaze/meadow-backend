package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.PlayerCrossServerInfo
import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.ServerPlayersResult

interface PlayerService {
    /** 服务器玩家列表（online / recent 筛选） */
    suspend fun getServerPlayers(
        serverId: Long,
        filter: String?,
        limit: Int,
    ): Response<ServerPlayersResult?>

    /** 玩家跨服详情 */
    suspend fun getPlayer(uuid: String): Response<PlayerCrossServerInfo?>
}
