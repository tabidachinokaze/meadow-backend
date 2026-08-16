package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 审核举报 */
@Serializable
data class HandleReportRequest(
    val action: String, // approve / reject
    @SerialName("handler_note")
    val handlerNote: String? = null,
)

/** 禁言玩家 */
@Serializable
data class BanPlayerRequest(
    @SerialName("player_uuid")
    val playerUuid: String,
    @SerialName("player_name")
    val playerName: String,
    @SerialName("duration_hours")
    val durationHours: Int,
    val reason: String = "违反聊天规则",
)
