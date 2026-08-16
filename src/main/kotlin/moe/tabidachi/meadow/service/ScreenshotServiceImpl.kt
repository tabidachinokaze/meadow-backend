package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.model.SystemRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.ReportRepository
import moe.tabidachi.meadow.repository.ScreenshotRepository
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import kotlin.uuid.Uuid

class ScreenshotServiceImpl(
    private val screenshotRepository: ScreenshotRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val reportRepository: ReportRepository,
) : ScreenshotService {

    override suspend fun getScreenshots(
        serverId: Long,
        uploaderId: Long?,
        status: String?,
    ): Response<List<ScreenshotInfo>?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val serverName = serverRepository.getServerInfo(serverId)?.name
        val list = screenshotRepository.getByServer(serverId, uploaderId, status)
            .map { it.toInfo(serverName) }
        return CommonStatusCode.SUCCESS.withData(list)
    }

    override suspend fun upload(
        callerId: Long,
        serverId: Long,
        bytes: ByteArray,
        contentType: String,
        description: String?,
        coordinates: String?,
    ): Response<ScreenshotInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (bytes.isEmpty() || bytes.size > 10 * 1024 * 1024) {
            return ScreenshotStatusCode.INVALID_IMAGE.emptyData()
        }
        val user = userRepository.getByUid(callerId)
            ?: return UserStatusCode.USER_NOT_FOUND.emptyData()
        val fileName = "${callerId}_${Uuid.random().toHexString()}.png"
        val url = try {
            storageService.uploadScreenshot(bytes, fileName, contentType)
        } catch (e: Exception) {
            return CommonStatusCode.FAILURE.emptyData(message = e.message)
        }
        val shot = screenshotRepository.add(
            serverId = serverId,
            uploaderId = callerId,
            uploaderName = user.username,
            imageUrl = url,
            description = description,
            coordinates = coordinates,
        )
        return CommonStatusCode.SUCCESS.withData(
            shot.toInfo(serverRepository.getServerInfo(serverId)?.name)
        )
    }

    override suspend fun download(serverId: Long, screenshotId: Long): Response<String?> {
        val shot = screenshotRepository.getById(serverId, screenshotId)
            ?: return ScreenshotStatusCode.SCREENSHOT_NOT_FOUND.emptyData()
        screenshotRepository.incrementDownload(serverId, screenshotId)
        return CommonStatusCode.SUCCESS.withData(shot.imageUrl)
    }

    override suspend fun report(
        callerId: Long,
        serverId: Long,
        screenshotId: Long,
        reason: String,
    ): Response<String?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val shot = screenshotRepository.getById(serverId, screenshotId)
            ?: return ScreenshotStatusCode.SCREENSHOT_NOT_FOUND.emptyData()
        screenshotRepository.markReported(serverId, screenshotId)
        // 写入举报记录（管理员审核用）
        reportRepository.add(screenshotId, callerId, reason)
        return CommonStatusCode.SUCCESS.emptyData<String>(message = "举报已提交，我们会尽快处理")
    }

    override suspend fun delete(callerId: Long, serverId: Long, screenshotId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val shot = screenshotRepository.getById(serverId, screenshotId)
            ?: return ScreenshotStatusCode.SCREENSHOT_NOT_FOUND.emptyData()
        val isManager = isOwnerOrAdmin(callerId, serverId) || isSystemAdmin(callerId)
        if (!isManager && shot.uploaderId != callerId) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (!screenshotRepository.delete(serverId, screenshotId)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(screenshotId)
    }

    override suspend fun getMyScreenshots(callerId: Long): Response<List<ScreenshotInfo>?> {
        val list = screenshotRepository.getByUploader(callerId).map { shot ->
            shot.toInfo(serverRepository.getServerInfo(shot.serverId)?.name)
        }
        return CommonStatusCode.SUCCESS.withData(list)
    }

    // ── 辅助 ──

    private suspend fun isSystemAdmin(userId: Long): Boolean =
        userRepository.getByUid(userId)?.role == SystemRole.ADMIN

    private suspend fun isOwnerOrAdmin(callerId: Long, serverId: Long): Boolean {
        val role = serverMemberRepository.getByServerAndUser(serverId, callerId)?.role
        return role == ServerRole.OWNER || role == ServerRole.ADMIN
    }

    private fun moe.tabidachi.meadow.database.model.Screenshot.toInfo(serverName: String?): ScreenshotInfo {
        return ScreenshotInfo(
            id = id,
            serverId = serverId,
            serverName = serverName,
            uploaderId = uploaderId,
            uploaderName = uploaderName,
            imageUrl = imageUrl,
            description = description,
            coordinates = coordinates,
            status = status,
            reportCount = reportCount,
            downloadCount = downloadCount,
            createdAt = createdAt,
        )
    }
}
