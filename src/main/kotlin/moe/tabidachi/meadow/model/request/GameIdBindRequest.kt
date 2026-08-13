package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * @param uuid player UUID
 * @param name player name
 */
@Serializable
data class GameIdBindRequest(
    val uuid: Uuid,
    val name: String,
    val code: String,
    @SerialName("machine_id")
    val machineId: String,
    @SerialName("server_key")
    val serverKey: String,
)