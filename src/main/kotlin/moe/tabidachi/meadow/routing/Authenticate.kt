package moe.tabidachi.meadow.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import moe.tabidachi.meadow.contract.RateLimitNames
import moe.tabidachi.meadow.exception.MissingParameterException
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.UserStatusCode
import moe.tabidachi.meadow.model.ValidStatusCode
import moe.tabidachi.meadow.model.request.CodeLoginRequest
import moe.tabidachi.meadow.model.request.PasswordLoginRequest
import moe.tabidachi.meadow.model.request.RegisterRequest
import moe.tabidachi.meadow.model.request.SendCodeType
import moe.tabidachi.meadow.service.AuthService

fun Route.authenticate() {
    val authService: AuthService by application.dependencies
    route("/auth") {
        post<RegisterRequest>("/register") { request ->
            call.respond(authService.register(request))
        }.describe {
            summary = "注册"
            requestBody {
                schema = jsonSchema<RegisterRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = buildString {
                        appendLine("code:")
                        listOf(
                            ValidStatusCode.INVALID_EMAIL,
                            UserStatusCode.PASSWORD_TOO_WEAK,
                            ValidStatusCode.INVALID_USERNAME,
                            UserStatusCode.EMAIL_ALREADY_EXISTS,
                            UserStatusCode.USERNAME_ALREADY_EXISTS,
                            UserStatusCode.GAME_ID_EXISTS,
                            UserStatusCode.VERIFICATION_CODE_ERROR,
                            UserStatusCode.VERIFICATION_CODE_EXPIRED,
                            UserStatusCode.SIGN_UP_SUCCESS
                        ).forEach {
                            appendLine("- ${it.code}: ${it.message}")
                        }
                        appendLine()
                        appendLine("data: token")
                    }
                    schema = jsonSchema<Response<String?>>()
                }
            }
        }
        post<PasswordLoginRequest>("/login/password") { request ->
            call.respond(authService.loginByPassword(request))
        }.describe {
            summary = "密码登录"
            requestBody {
                schema = jsonSchema<PasswordLoginRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = buildString {
                        appendLine("code:")
                        listOf(
                            UserStatusCode.USER_NOT_REGISTERED,
                            UserStatusCode.PASSWORD_TOO_WEAK,
                            UserStatusCode.PASSWORD_INCORRECT,
                            UserStatusCode.LOGIN_SUCCESS
                        ).forEach {
                            appendLine("- ${it.code}: ${it.message}")
                        }
                        appendLine()
                        appendLine("data: token")
                    }
                    schema = jsonSchema<Response<String?>>()
                }
            }
        }
        post<CodeLoginRequest>("/login/code") { request ->
            call.respond(authService.loginByCode(request))
        }.describe {
            summary = "验证码登录"
            requestBody {
                schema = jsonSchema<CodeLoginRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = buildString {
                        appendLine("code:")
                        listOf(
                            ValidStatusCode.INVALID_EMAIL,
                            UserStatusCode.VERIFICATION_CODE_ERROR,
                            UserStatusCode.VERIFICATION_CODE_EXPIRED,
                            UserStatusCode.USER_NOT_REGISTERED,
                            UserStatusCode.LOGIN_SUCCESS,
                        ).forEach {
                            appendLine("- ${it.code}: ${it.message}")
                        }
                        appendLine()
                        appendLine("data: token")
                    }
                    schema = jsonSchema<Response<String?>>()
                }
            }
        }
    }

    rateLimit(RateLimitNames.email) {
        post("/send-code") {
            val email = call.request.queryParameters["email"] ?: throw MissingParameterException("email")
            val type = call.request.queryParameters["type"] ?: throw MissingParameterException("type")
            val response = when (SendCodeType.valueOf(type)) {
                SendCodeType.REGISTER -> authService.sendRegisterCode(email)
                SendCodeType.LOGIN -> authService.sendLoginCode(email)
                SendCodeType.RESET_PASSWORD -> TODO()
            }
            call.respond(response)
        }.describe {
            summary = "发送验证码"
            parameters {
                query("email") {
                    description = "邮箱"
                    required = true
                }
                query("type") {
                    description = buildString {
                        appendLine("验证码类型：")
                        SendCodeType.entries.forEach {
                            when (it) {
                                SendCodeType.REGISTER -> appendLine("- ${it.name}: 注册")
                                SendCodeType.LOGIN -> appendLine("- ${it.name}: 登录")
                                SendCodeType.RESET_PASSWORD -> appendLine("- ${it.name}: 重置密码")
                            }
                        }
                    }
                    schema = jsonSchema<SendCodeType>()
                    required = true
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = buildString {
                        appendLine("code:")
                        listOf(
                            ValidStatusCode.INVALID_EMAIL,
                            UserStatusCode.EMAIL_ALREADY_EXISTS,
                            UserStatusCode.USER_NOT_REGISTERED,
                            CommonStatusCode.SUCCESS,
                            CommonStatusCode.INTERNAL_SERVER_ERROR
                        ).forEach {
                            appendLine("- ${it.code}: ${it.message}")
                        }
                    }
                }
            }
        }
    }
}
