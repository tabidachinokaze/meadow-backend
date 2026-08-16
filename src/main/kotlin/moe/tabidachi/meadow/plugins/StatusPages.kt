package moe.tabidachi.meadow.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.emptyData

fun Application.configureStatusPages() {
    install(StatusPages) {
        // 客户端参数缺失/格式错误 → 40000 请求参数校验失败（已知问题 #13）
        exception<MissingRequestParameterException> { call, cause ->
            call.respond(
                CommonStatusCode.PARAM_ERROR.emptyData<String>(
                    message = "缺少参数: ${cause.parameterName}"
                )
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respond(
                CommonStatusCode.PARAM_ERROR.emptyData<String>(
                    message = cause.message
                )
            )
        }
        exception<SerializationException> { call, cause ->
            call.respond(
                CommonStatusCode.PARAM_ERROR.emptyData<String>(
                    message = "请求体格式错误"
                )
            )
        }
        exception<Throwable> { call, cause ->
            // 仅记录详情到服务端日志，对客户端返回固定文案（不泄露内部异常/SQL 细节）
            call.application.environment.log.error("Unhandled exception: ${cause.message}", cause)
            call.respond(
                CommonStatusCode.INTERNAL_SERVER_ERROR.emptyData<String>(
                    message = "系统繁忙，请稍后再试"
                )
            )
        }
        status(HttpStatusCode.NotFound) {
            call.respond(CommonStatusCode.NOT_FOUND.emptyData<String>())
        }
        status(HttpStatusCode.Unauthorized) {
            call.respond(CommonStatusCode.UNAUTHORIZED.emptyData<String>())
        }
    }
}