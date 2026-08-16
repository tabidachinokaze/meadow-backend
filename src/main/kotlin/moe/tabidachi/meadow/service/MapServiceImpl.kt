package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.model.SystemRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.MapConfigRequest
import moe.tabidachi.meadow.repository.MapConfigRepository
import moe.tabidachi.meadow.repository.PlayerPositionRepository
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import kotlin.time.Clock

class MapServiceImpl(
    private val mapConfigRepository: MapConfigRepository,
    private val playerPositionRepository: PlayerPositionRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository,
) : MapService {

    override suspend fun getConfig(serverId: Long): Response<MapConfigInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val config = mapConfigRepository.getByServer(serverId)
            ?: return MapStatusCode.CONFIG_NOT_SET.emptyData()
        return CommonStatusCode.SUCCESS.withData(config.toInfo())
    }

    override suspend fun saveConfig(
        adminId: Long,
        serverId: Long,
        request: MapConfigRequest,
    ): Response<MapConfigInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrAdmin(adminId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        val saved = mapConfigRepository.upsert(
            serverId = serverId,
            type = request.type,
            tileUrl = request.tileUrl,
            worldName = request.worldName,
            centerX = request.center?.x ?: 0,
            centerZ = request.center?.z ?: 0,
            zoomMin = request.zoom?.min ?: 0,
            zoomMax = request.zoom?.max ?: 3,
            zoomDefault = request.zoom?.default ?: 1,
            playerMarkersUrl = request.playerMarkersUrl,
        )
        return CommonStatusCode.SUCCESS.withData(saved.toInfo())
    }

    override suspend fun getPlayers(serverId: Long): Response<MapPlayersResult?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val positions = playerPositionRepository.getByServer(serverId)
        val updatedAt = positions.maxOfOrNull { it.updatedAt }
        // 头像：按 game_id 关联用户头像（匹配不到为 null）
        val players = positions.map { pos ->
            val avatarUrl = userRepository.getByGameId(pos.gameUuid)?.avatarUrl
            MapPlayerPosition(
                name = pos.playerName,
                uuid = pos.gameUuid,
                x = pos.x,
                y = pos.y,
                z = pos.z,
                world = pos.world,
                avatarUrl = avatarUrl,
            )
        }
        return CommonStatusCode.SUCCESS.withData(
            MapPlayersResult(players = players, updatedAt = updatedAt)
        )
    }

    // ── 辅助 ──

    private suspend fun isOwnerOrAdmin(userId: Long, serverId: Long): Boolean {
        if (userRepository.getByUid(userId)?.role == SystemRole.ADMIN) return true
        val role = serverMemberRepository.getByServerAndUser(serverId, userId)?.role
        return role == ServerRole.OWNER || role == ServerRole.ADMIN
    }

    private fun moe.tabidachi.meadow.database.model.MapConfig.toInfo(): MapConfigInfo =
        MapConfigInfo(
            type = type,
            tileUrl = tileUrl,
            worldName = worldName,
            center = MapCenter(x = centerX, z = centerZ),
            zoom = MapZoom(min = zoomMin, max = zoomMax, default = zoomDefault),
            playerMarkersUrl = playerMarkersUrl,
            updatedAt = updatedAt,
        )
}
