package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.ScreenshotInfo

interface ScreenshotService {
    /** 获取服务器截图列表（支持按上传者 / 状态筛选） */
    suspend fun getScreenshots(
        serverId: Long,
        uploaderId: Long? = null,
        status: String? = null,
    ): Response<List<ScreenshotInfo>?>

    /** 上传截图（登录即可，写入 S3） */
    suspend fun upload(
        callerId: Long,
        serverId: Long,
        bytes: ByteArray,
        contentType: String,
        description: String?,
        coordinates: String?,
    ): Response<ScreenshotInfo?>

    /** 下载截图（返回图片 URL，download_count+1） */
    suspend fun download(serverId: Long, screenshotId: Long): Response<String?>

    /** 举报截图（标记 reported + report_count+1） */
    suspend fun report(callerId: Long, serverId: Long, screenshotId: Long, reason: String): Response<String?>

    /** 删除截图（owner/admin/系统管理员 或 上传者本人） */
    suspend fun delete(callerId: Long, serverId: Long, screenshotId: Long): Response<Long?>

    /** 我的截图（个人中心） */
    suspend fun getMyScreenshots(callerId: Long): Response<List<ScreenshotInfo>?>
}
