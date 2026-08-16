package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.BanInfo
import moe.tabidachi.meadow.model.ReportInfo
import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.request.BanPlayerRequest
import moe.tabidachi.meadow.model.request.HandleReportRequest

interface AdminService {
    /** 举报列表（按状态筛选，默认 pending） */
    suspend fun getReports(status: String?): Response<List<ReportInfo>?>

    /** 审核举报（approve → 截图下架；reject → 驳回） */
    suspend fun handleReport(
        handlerId: Long,
        reportId: Long,
        request: HandleReportRequest,
    ): Response<ReportInfo?>

    /** 禁言玩家（服务器 owner/admin / 系统管理员） */
    suspend fun banPlayer(
        adminId: Long,
        serverId: Long,
        request: BanPlayerRequest,
    ): Response<BanInfo?>

    /** 禁言列表（服务器 owner/admin / 系统管理员） */
    suspend fun getBans(adminId: Long, serverId: Long): Response<List<BanInfo>?>

    /** 解除禁言（服务器 owner/admin / 系统管理员） */
    suspend fun unbanPlayer(adminId: Long, serverId: Long, banId: Long): Response<Long?>
}
