package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.BanPlayerRequest
import moe.tabidachi.meadow.model.request.HandleReportRequest
import moe.tabidachi.meadow.repository.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class AdminServiceImpl(
    private val reportRepository: ReportRepository,
    private val banRepository: BanRepository,
    private val screenshotRepository: ScreenshotRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository,
    private val permissionGuard: PermissionGuard,
    private val database: org.jetbrains.exposed.v1.jdbc.Database,
) : AdminService {

    override suspend fun getReports(handlerId: Long, status: String?): Response<List<ReportInfo>?> {
        // 鉴权：仅拥有截图管理权限的成员可查看举报（防止任意登录用户拉全站举报，审计 C3）
        val visibleServerIds = serverMemberRepository.getByUser(handlerId)
            .filter { it.role == moe.tabidachi.meadow.database.model.ServerRole.OWNER || it.permissions.canManageScreenshots }
            .map { it.serverId }
            .toSet()
        if (visibleServerIds.isEmpty()) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        val list = reportRepository.getByStatus(status)
            .filter { report ->
                val serverId = screenshotRepository.getByScreenshotId(report.screenshotId)?.serverId
                serverId != null && serverId in visibleServerIds
            }
            .map { report ->
                val screenshot = screenshotRepository.getByScreenshotId(report.screenshotId)
                ReportInfo(
                    id = report.id,
                    screenshotId = report.screenshotId,
                    screenshotUrl = screenshot?.imageUrl,
                    serverId = screenshot?.serverId,
                    serverName = screenshot?.serverId?.let { serverRepository.getServerInfo(it)?.name },
                    reporterId = report.reporterId,
                    reporterName = userRepository.getUserInfo(report.reporterId)?.username,
                    reason = report.reason,
                    status = report.status,
                    handlerId = report.handlerId,
                    handlerNote = report.handlerNote,
                    createdAt = report.createdAt,
                    handledAt = report.handledAt,
                )
            }
        return CommonStatusCode.SUCCESS.withData(list)
    }

    override suspend fun handleReport(
        handlerId: Long,
        reportId: Long,
        request: HandleReportRequest,
    ): Response<ReportInfo?> {
        val report = reportRepository.getById(reportId)
            ?: return AdminStatusCode.REPORT_NOT_FOUND.emptyData()
        if (report.status != "pending") {
            return AdminStatusCode.ALREADY_HANDLED.emptyData()
        }
        val screenshot = screenshotRepository.getByScreenshotId(report.screenshotId)
        val serverId = screenshot?.serverId
        if (serverId != null && !permissionGuard.requirePermission(serverId, handlerId, PermissionBit.CAN_MANAGE_SCREENSHOTS)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        val newStatus = when (request.action) {
            "approve" -> {
                // 服务级事务：截图下架（保留证据，标记 deleted）+ 举报标记处理原子完成
                database.withTransaction {
                    if (screenshot != null) {
                        screenshotRepository.markDeleted(screenshot.serverId, screenshot.id)
                    }
                    if (!reportRepository.handle(reportId, "approved", handlerId, request.handlerNote)) {
                        throw IllegalStateException("handle report failed")
                    }
                }
                "approved"
            }

            "reject" -> {
                if (!reportRepository.handle(reportId, "rejected", handlerId, request.handlerNote)) {
                    return CommonStatusCode.FAILURE.emptyData()
                }
                "rejected"
            }

            else -> return AdminStatusCode.INVALID_ACTION.emptyData()
        }
        val updated = reportRepository.getById(reportId) ?: return CommonStatusCode.FAILURE.emptyData()
        return CommonStatusCode.SUCCESS.withData(
            ReportInfo(
                id = updated.id,
                screenshotId = updated.screenshotId,
                screenshotUrl = screenshot?.imageUrl,
                serverId = serverId,
                serverName = serverId?.let { serverRepository.getServerInfo(it)?.name },
                reporterId = updated.reporterId,
                reporterName = userRepository.getUserInfo(updated.reporterId)?.username,
                reason = updated.reason,
                status = updated.status,
                handlerId = updated.handlerId,
                handlerNote = updated.handlerNote,
                createdAt = updated.createdAt,
                handledAt = updated.handledAt,
            )
        )
    }

    override suspend fun banPlayer(
        adminId: Long,
        serverId: Long,
        request: BanPlayerRequest,
    ): Response<BanInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!permissionGuard.requirePermission(serverId, adminId, PermissionBit.CAN_MANAGE_SCREENSHOTS)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (request.durationHours == 0 || request.durationHours < -1) {
            return AdminStatusCode.INVALID_DURATION.emptyData()
        }
        val expiresAt = if (request.durationHours == -1) null else Clock.System.now().plus(request.durationHours.hours)
        val ban = banRepository.add(
            serverId = serverId,
            playerUuid = request.playerUuid,
            playerName = request.playerName,
            bannedBy = adminId,
            reason = request.reason,
            durationHours = request.durationHours,
            expiresAt = expiresAt,
        )
        return CommonStatusCode.SUCCESS.withData(ban.toInfo())
    }

    override suspend fun getBans(adminId: Long, serverId: Long): Response<List<BanInfo>?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!permissionGuard.requirePermission(serverId, adminId, PermissionBit.CAN_MANAGE_SCREENSHOTS)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(
            banRepository.getByServer(serverId).filter { it.isActive }.map { it.toInfo() }
        )
    }

    override suspend fun unbanPlayer(adminId: Long, serverId: Long, banId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!permissionGuard.requirePermission(serverId, adminId, PermissionBit.CAN_MANAGE_SCREENSHOTS)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (banRepository.getById(serverId, banId) == null) {
            return AdminStatusCode.BAN_NOT_FOUND.emptyData()
        }
        if (!banRepository.deactivate(serverId, banId)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(banId)
    }

    // ── 辅助 ──

    private fun moe.tabidachi.meadow.database.model.Ban.toInfo(): BanInfo {
        return BanInfo(
            id = id,
            serverId = serverId,
            playerUuid = playerUuid,
            playerName = playerName,
            reason = reason,
            durationHours = durationHours,
            expiresAt = expiresAt,
            isActive = isActive,
            createdAt = createdAt,
        )
    }
}
