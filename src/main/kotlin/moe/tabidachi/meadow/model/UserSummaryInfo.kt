package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable

/** 个人中心活跃数据统计（/users/me/summary） */
@Serializable
data class UserSummaryInfo(
    /** 收藏服务器数 */
    val favoriteCount: Long,
    /** 上传截图数 */
    val screenshotCount: Long,
    /** 发言条数（按游戏 ID 关联） */
    val messageCount: Long,
    /** 累计在线时长（秒，按游戏 ID 关联） */
    val totalOnlineSeconds: Long,
)
