package moe.tabidachi.meadow.model

/**
 * 截图模块状态码（码段 405xx）
 */
enum class ScreenshotStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    SCREENSHOT_NOT_FOUND(40501, "截图不存在"),
    INVALID_IMAGE(40502, "图片无效或超过大小限制"),
}
