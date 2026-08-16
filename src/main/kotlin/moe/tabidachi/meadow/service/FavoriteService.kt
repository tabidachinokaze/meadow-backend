package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.ServerInfo
import kotlinx.serialization.Serializable

/** 收藏结果 */
@Serializable
data class FavoriteResult(
    val isFavorited: Boolean,
)

interface FavoriteService {
    /** 收藏服务器（幂等） */
    suspend fun addFavorite(callerId: Long, serverId: Long): Response<FavoriteResult?>

    /** 取消收藏（幂等） */
    suspend fun removeFavorite(callerId: Long, serverId: Long): Response<FavoriteResult?>

    /** 我的收藏服务器列表（脱敏） */
    suspend fun getFavorites(callerId: Long): Response<List<ServerInfo>?>
}
