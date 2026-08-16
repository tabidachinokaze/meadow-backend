package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.ServerStatusRequest
import moe.tabidachi.meadow.repository.ModRepository
import moe.tabidachi.meadow.repository.PlayerPositionRepository
import moe.tabidachi.meadow.repository.ServerPlayerRepository
import moe.tabidachi.meadow.repository.ServerRepository
import kotlin.time.Clock

class ServerStatusServiceImpl(
    private val serverRepository: ServerRepository,
    private val serverPlayerRepository: ServerPlayerRepository,
    private val modRepository: ModRepository,
    private val playerPositionRepository: PlayerPositionRepository,
) : ServerStatusService {

    override suspend fun report(serverId: Long, request: ServerStatusRequest): Response<ServerStatusResult?> {
        val server = serverRepository.getById(serverId)
            ?: return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        if (server.serverKey != request.serverKey) {
            return ServerStatusCode.SERVER_KEY_ERROR.emptyData()
        }
        if (server.machineId != request.machineId) {
            return ServerStatusCode.ENVIRONMENT_CHANGED.emptyData()
        }

        val now = Clock.System.now()
        // 更新服务器状态字段
        val updated = serverRepository.update(
            serverId = serverId,
            onlinePlayers = request.onlinePlayers,
            maxPlayers = request.maxPlayers,
            uptimeSeconds = request.uptimeSeconds,
            lastStatusAt = now,
        )
        if (!updated) {
            return CommonStatusCode.FAILURE.emptyData()
        }

        // 同步在线玩家
        val onlinePlayers = serverPlayerRepository.syncOnline(
            serverId = serverId,
            online = request.players.map { it.uuid to it.name },
            now = now,
        )

        // 同步玩家实时位置（Agent 上报携带坐标时；地图模块 §9.4.2 数据源）
        request.players.filter { it.x != null && it.y != null && it.z != null }.forEach { p ->
            playerPositionRepository.upsert(
                serverId = serverId,
                gameUuid = p.uuid,
                playerName = p.name,
                x = p.x!!,
                y = p.y!!,
                z = p.z!!,
                world = p.world,
                now = now,
            )
        }

        // 同步 Mod 列表（Agent 全量上报时更新）
        if (request.mods.isNotEmpty()) {
            modRepository.syncMods(
                serverId = serverId,
                mods = request.mods.map { Triple(it.name, it.version, it.category) },
            )
        }

        return CommonStatusCode.SUCCESS.withData(
            ServerStatusResult(
                onlinePlayers = request.onlinePlayers,
                maxPlayers = request.maxPlayers,
                onlinePlayerCount = onlinePlayers.size,
                syncedAt = now,
            )
        )
    }
}
