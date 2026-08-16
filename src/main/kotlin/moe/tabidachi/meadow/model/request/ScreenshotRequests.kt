package moe.tabidachi.meadow.model.request

import kotlinx.serialization.Serializable

/** 举报截图 */
@Serializable
data class ReportScreenshotRequest(
    val reason: String,
)
