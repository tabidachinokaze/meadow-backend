package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.model.SystemRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.ModpackRepository
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import java.security.MessageDigest
import kotlin.uuid.Uuid

class ModpackServiceImpl(
    private val modpackRepository: ModpackRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
) : ModpackService {

    override suspend fun getModpack(serverId: Long): Response<ModpackInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val active = modpackRepository.getActive(serverId)
        return CommonStatusCode.SUCCESS.withData(active?.toInfo(withPresignedUrl = true))
    }

    override suspend fun download(serverId: Long): Response<String?> {
        val active = modpackRepository.getActive(serverId)
            ?: return ModpackStatusCode.MODPACK_NOT_FOUND.emptyData()
        modpackRepository.incrementDownload(serverId, active.id)
        // 私有桶：返回预签名 URL
        val url = runCatching { storageService.presignObjectUrl(active.downloadUrl) }.getOrNull()
        return if (url != null) {
            CommonStatusCode.SUCCESS.withData(url)
        } else {
            CommonStatusCode.FAILURE.emptyData(message = "文件存储地址无效")
        }
    }

    override suspend fun downloadStream(serverId: Long): Pair<ByteArray, String>? {
        val active = modpackRepository.getActive(serverId)
            ?: return null
        modpackRepository.incrementDownload(serverId, active.id)
        val (bucket, key) = storageService.splitObjectUrl(active.downloadUrl) ?: return null
        val bytes = runCatching { storageService.downloadObject(bucket, key) }.getOrNull() ?: return null
        val fileName = "modpack-${active.version}.zip".replace("\"", "")
        return bytes to fileName
    }

    override suspend fun update(
        callerId: Long,
        serverId: Long,
        bytes: ByteArray,
        contentType: String,
        version: String,
        releaseDate: String,
        changelog: String?,
    ): Response<ModpackInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!isOwnerOrAdmin(callerId, serverId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (version.isBlank()) {
            return ModpackStatusCode.INVALID_VERSION.emptyData()
        }
        if (bytes.isEmpty() || bytes.size > 2L * 1024 * 1024 * 1024) {
            return ModpackStatusCode.INVALID_FILE.emptyData()
        }
        val fileName = "mp_${serverId}_${Uuid.random().toHexString()}.zip"
        val url = try {
            storageService.uploadFile("modpacks", bytes, fileName, contentType)
        } catch (e: Exception) {
            return CommonStatusCode.FAILURE.emptyData(message = e.message)
        }
        val md5 = MessageDigest.getInstance("MD5").digest(bytes)
            .joinToString("") { "%02x".format(it) }
        val modpack = modpackRepository.add(
            serverId = serverId,
            version = version,
            releaseDate = releaseDate,
            downloadUrl = url,
            fileSize = bytes.size.toLong(),
            md5Hash = md5,
            changelog = changelog,
        )
        // 新版本激活，旧版本失效
        modpackRepository.deactivateOthers(serverId, modpack.id)
        return CommonStatusCode.SUCCESS.withData(modpack.toInfo())
    }

    // ── 辅助 ──

    private suspend fun isOwnerOrAdmin(callerId: Long, serverId: Long): Boolean {
        if (userRepository.getByUid(callerId)?.role == SystemRole.ADMIN) return true
        val role = serverMemberRepository.getByServerAndUser(serverId, callerId)?.role
        return role == ServerRole.OWNER || role == ServerRole.ADMIN
    }

    private suspend fun moe.tabidachi.meadow.database.model.ServerModpack.toInfo(
        withPresignedUrl: Boolean = false,
    ): ModpackInfo {
        val effectiveUrl = if (withPresignedUrl) {
            runCatching { storageService.presignObjectUrl(downloadUrl) }.getOrNull() ?: downloadUrl
        } else {
            downloadUrl
        }
        return ModpackInfo(
            id = id,
            serverId = serverId,
            version = version,
            releaseDate = releaseDate,
            downloadUrl = effectiveUrl,
            fileSize = fileSize,
            md5Hash = md5Hash,
            changelog = changelog,
            downloadCount = downloadCount,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
