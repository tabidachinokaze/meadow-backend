package moe.tabidachi.meadow.model

interface StatusCode {
    val code: Int
    val message: String
}

enum class CommonStatusCode(
    override val code: Int,
    override val message: String
) : StatusCode {
    SUCCESS(20000, "操作成功"),
    FAILURE(30000, "操作失败"),
    PARAM_ERROR(40000, "请求参数校验失败"),
    UNAUTHORIZED(40101, "请先登录或 Token 已失效"),
    FORBIDDEN(40102, "没有权限访问该资源"),
    NOT_FOUND(40103, "资源不存在"),
    INTERNAL_SERVER_ERROR(50000, "系统繁忙，请稍后再试")
}

// 用户模块状态码 (02)
enum class UserStatusCode(
    override val code: Int,
    override val message: String
) : StatusCode {
    USER_NOT_FOUND(40201, "用户不存在"),
    PASSWORD_INCORRECT(40202, "密码错误"),
    USER_DISABLED(40203, "账号已被禁用"),
    PHONE_ALREADY_REGISTERED(40204, "该手机号已被注册"),
    EMAIL_ALREADY_EXISTS(40205, "邮箱已注册"),
    USERNAME_ALREADY_EXISTS(40206, "用户名已被占用"),
    PASSWORD_TOO_WEAK(40207, "密码长度必须至少为8个字符"),
    USER_NOT_REGISTERED(40208, "用户未注册"),
    SIGN_UP_SUCCESS(40209, "注册成功"),
    SIGN_UP_FAILURE(40210, "注册失败"),
    LOGIN_SUCCESS(40211, "登录成功"),
    LOGIN_FAILURE(40212, "登录失败"),
    GAME_ID_EXISTS(40213, "游戏ID已被占用"),
    VERIFICATION_CODE_ERROR(40214, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(40215, "验证码已过期"),
}

enum class ServerStatusCode(
    override val code: Int,
    override val message: String
) : StatusCode {
    SERVER_ALREADY_EXISTS(40301, "服务器已存在"),
    SERVER_NOT_EXISTS(40302, "服务器不存在"),
    WITHOUT_ANY_FIELDS(40303, "没有要更新的字段"),
    SERVER_KEY_ERROR(40304, "服务器密钥错误"),
    ENVIRONMENT_CHANGED(40305, "服务器环境变更，请重新初始化"),
    BIND_FAILURE(40306, "绑定失败，请使用本人账号绑定"),
}

// 验证错误状态码 (04) —— 原 403xx 与 ServerStatusCode 冲突，迁至 404xx（已知问题 #3）
enum class ValidStatusCode(
    override val code: Int,
    override val message: String
) : StatusCode {
    INVALID_EMAIL(40401, "Invalid email format"),
    INVALID_PHONE(40402, "Invalid phone number"),
    INVALID_USERNAME(40403, "Invalid username format"),
}

inline fun <reified T> StatusCode.withData(data: T): Response<T> =
    Response(code = code, message = message, data = data)

inline fun <reified T> StatusCode.emptyData(code: Int = this.code, message: String? = this.message): Response<T?> =
    Response(code = code, message = message ?: this.message, data = null)

val Response<*>.statusCode: StatusCode?
    get() = (CommonStatusCode.entries + UserStatusCode.entries + ValidStatusCode.entries + ServerStatusCode.entries)
        .find { it.code == code }
