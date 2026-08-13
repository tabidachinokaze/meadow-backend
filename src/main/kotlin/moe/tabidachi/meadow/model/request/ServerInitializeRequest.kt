package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerInitializeRequest(
    @SerialName("server_key")
    val serverKey: String,
    @SerialName("machine_id")
    val machineId: String
)
