package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.model.SystemRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.repository.WorldRepository
import kotlin.uuid.Uuid

class WorldServiceImpl(
    private val worldRepository: WorldRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
) : WorldService {

    override suspend fun getWorlds(serverId: Long): Response<List<WorldInfo>?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val list = worldRepository.getByServer(serverId).map { it.toInfo() }
        return CommonStatusCode.SUCCESS.withData(list)
    }

    override suspend fun upload(
        callerId: Long,
        serverId: Long,
        bytes: ByteArray,
        contentType: String,
        worldName: String,
        worldType: String,
    ): Response<WorldInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrAdmin(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (bytes.isEmpty() || bytes.size > 2L * 1024 * 1024 * 1024) {
            return WorldStatusCode.INVALID_FILE.emptyData()
        }
        val fileName = "w_${serverId}_${Uuid.random().toHexString()}.zip"
        val url = try {
            storageService.uploadFile("worlds", bytes, fileName, contentType)
        } catch (e: Exception) {
            return CommonStatusCode.FAILURE.emptyData(message = e.message)
        }
        val firstWorld = worldRepository.getByServer(serverId).isEmpty()
        val world = worldRepository.add(
            serverId = serverId,
            worldName = worldName,
            worldType = worldType,
            fileSize = bytes.size.toLong(),
            isCurrent = firstWorld,
            downloadUrl = url,
        )
        return CommonStatusCode.SUCCESS.withData(world.toInfo())
    }

    override suspend fun download(serverId: Long, worldId: Long): Response<String?> {
        val world = worldRepository.getById(serverId, worldId)
            ?: return WorldStatusCode.WORLD_NOT_FOUND.emptyData()
        worldRepository.incrementDownload(serverId, worldId)
        // 私有桶：返回预签名 URL 供前端下载
        val url = world.downloadUrl?.let { runCatching { storageService.presignObjectUrl(it) }.getOrNull() }
        return if (url != null) {
            CommonStatusCode.SUCCESS.withData(url)
        } else {
            CommonStatusCode.FAILURE.emptyData(message = "文件存储地址无效")
        }
    }

    override suspend fun downloadStream(serverId: Long, worldId: Long): Pair<ByteArray, String>? {
        val world = worldRepository.getById(serverId, worldId)
            ?: return null
        worldRepository.incrementDownload(serverId, worldId)
        val (bucket, key) = storageService.splitObjectUrl(world.downloadUrl ?: return null) ?: return null
        val bytes = runCatching { storageService.downloadObject(bucket, key) }.getOrNull() ?: return null
        val fileName = "${world.worldName}.zip".replace("\"", "")
        return bytes to fileName
    }

    override suspend fun setCurrent(callerId: Long, serverId: Long, worldId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrAdmin(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (worldRepository.getById(serverId, worldId) == null) {
            return WorldStatusCode.WORLD_NOT_FOUND.emptyData()
        }
        if (!worldRepository.setCurrent(serverId, worldId)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(worldId)
    }

    override suspend fun delete(callerId: Long, serverId: Long, worldId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrAdmin(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (worldRepository.getById(serverId, worldId) == null) {
            return WorldStatusCode.WORLD_NOT_FOUND.emptyData()
        }
        if (!worldRepository.delete(serverId, worldId)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(worldId)
    }

    // ── 辅助 ──

    private suspend fun isOwnerOrAdmin(callerId: Long, serverId: Long): Boolean {
        if (userRepository.getByUid(callerId)?.role == SystemRole.ADMIN) return true
        val role = serverMemberRepository.getByServerAndUser(serverId, callerId)?.role
        return role == ServerRole.OWNER || role == ServerRole.ADMIN
    }

    private fun moe.tabidachi.meadow.database.model.WorldSave.toInfo(): WorldInfo {
        return WorldInfo(
            id = id,
            serverId = serverId,
            worldName = worldName,
            worldType = worldType,
            fileSize = fileSize,
            isCurrent = isCurrent,
            lastSaved = lastSaved,
            downloadCount = downloadCount,
            downloadUrl = downloadUrl,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
