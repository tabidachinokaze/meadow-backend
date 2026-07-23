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
    PARAM_ERROR(40000, "请求参数校验失败"),
    UNAUTHORIZED(40101, "请先登录或 Token 已失效"),
    FORBIDDEN(40102, "没有权限访问该资源"),
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
}

// 验证错误状态码 (03)
enum class ValidStatusCode(
    override val code: Int,
    override val message: String
) : StatusCode {
    INVALID_EMAIL(40301, "Invalid email format"),
    INVALID_PHONE(40302, "Invalid phone number"),
    INVALID_USERNAME(40303, "Invalid username format"),
}

inline fun <reified T> StatusCode.withData(data: T): Response<T> =
    Response(code = code, message = message, data = data)

fun StatusCode.emptyData(code: Int = this.code, message: String? = this.message): Response<String?> =
    Response(code = code, message = message ?: this.message, data = null)

val Response<*>.statusCode: StatusCode? get() = (CommonStatusCode.entries + UserStatusCode.entries + ValidStatusCode.entries).find { it.code == code }
