package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.ModpackInfo
import moe.tabidachi.meadow.model.Response

interface ModpackService {
    /** 当前激活的整合包信息 */
    suspend fun getModpack(serverId: Long): Response<ModpackInfo?>

    /** 下载整合包（URL + 计数） */
    suspend fun download(serverId: Long): Response<String?>

    /** 更新整合包（owner / admin，multipart，计算 MD5） */
    suspend fun update(
        callerId: Long,
        serverId: Long,
        bytes: ByteArray,
        contentType: String,
        version: String,
        releaseDate: String,
        changelog: String?,
    ): Response<ModpackInfo?>
}
