package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.request.ServerStatusRequest

interface ServerStatusService {
    /** 处理 Agent 定时状态上报：认证 → 更新服务器状态 → 同步在线玩家 */
    suspend fun report(serverId: Long, request: ServerStatusRequest): Response<ServerStatusResult?>
}

/** 上报结果（供调试/监控） */
@kotlinx.serialization.Serializable
data class ServerStatusResult(
    val onlinePlayers: Int,
    val maxPlayers: Int,
    val onlinePlayerCount: Int,
    val syncedAt: kotlin.time.Instant,
)
