package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.ServerPlayerRepository
import moe.tabidachi.meadow.repository.ServerRepository

class PlayerServiceImpl(
    private val serverPlayerRepository: ServerPlayerRepository,
    private val serverRepository: ServerRepository,
) : PlayerService {

    override suspend fun getServerPlayers(
        serverId: Long,
        filter: String?,
        limit: Int,
    ): Response<ServerPlayersResult?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val online = serverPlayerRepository.getOnline(serverId).map { it.toInfo() }
        val recentOffline = if (filter == "online") {
            emptyList()
        } else {
            serverPlayerRepository.getRecentOffline(serverId, limit).map { it.toInfo() }
        }
        return CommonStatusCode.SUCCESS.withData(
            ServerPlayersResult(
                onlineCount = online.size,
                online = online,
                recentOffline = recentOffline,
            )
        )
    }

    override suspend fun getPlayer(uuid: String): Response<PlayerCrossServerInfo?> {
        val records = serverPlayerRepository.getByUuid(uuid)
        if (records.isEmpty()) {
            return PlayerStatusCode.PLAYER_NOT_FOUND.emptyData()
        }
        val name = records.first().playerName
        val servers = records.map { record ->
            PlayerCrossServerInfo.ServerPlayerEntry(
                serverId = record.serverId,
                serverName = serverRepository.getServerInfo(record.serverId)?.name,
                firstSeen = record.firstSeen,
                lastSeen = record.lastSeen,
                onlineDuration = record.onlineDuration,
                isOnline = record.isOnline,
            )
        }
        return CommonStatusCode.SUCCESS.withData(
            PlayerCrossServerInfo(
                gameUuid = uuid,
                playerName = name,
                avatarUrl = playerAvatarUrl(name),
                servers = servers,
            )
        )
    }

    private fun moe.tabidachi.meadow.database.model.ServerPlayer.toInfo(): PlayerInfo {
        return PlayerInfo(
            gameUuid = gameUuid,
            playerName = playerName,
            avatarUrl = playerAvatarUrl(playerName),
            isOnline = isOnline,
            firstSeen = firstSeen,
            lastSeen = lastSeen,
            onlineDuration = onlineDuration,
        )
    }
}
