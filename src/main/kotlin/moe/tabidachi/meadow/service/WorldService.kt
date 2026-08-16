package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.WorldInfo

interface WorldService {
    /** 服务器存档列表 */
    suspend fun getWorlds(serverId: Long): Response<List<WorldInfo>?>

    /** 上传存档（owner / admin，S3 存储） */
    suspend fun upload(
        callerId: Long,
        serverId: Long,
        bytes: ByteArray,
        contentType: String,
        worldName: String,
        worldType: String,
    ): Response<WorldInfo?>

    /** 下载存档（返回 URL，计数+1） */
    suspend fun download(serverId: Long, worldId: Long): Response<String?>

    /** 流式下载存档（从 S3 读取字节，计数+1）；返回 null 表示存档不存在 */
    suspend fun downloadStream(serverId: Long, worldId: Long): Pair<ByteArray, String>?

    /** 标记当前世界（owner / admin） */
    suspend fun setCurrent(callerId: Long, serverId: Long, worldId: Long): Response<Long?>

    /** 删除存档（owner / admin） */
    suspend fun delete(callerId: Long, serverId: Long, worldId: Long): Response<Long?>
}
