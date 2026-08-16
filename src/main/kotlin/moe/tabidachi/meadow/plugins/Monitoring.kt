package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(DoubleReceive)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        // 仅记录请求方法/路径/状态/耗时，不打印请求/响应体（避免密码/token/验证码入日志）
        format { call ->
            val status = call.response.status()?.value ?: 0
            "HTTP ${call.request.httpMethod.value} ${call.request.path()} -> $status (${call.processingTimeMillis()}ms)"
        }
    }
}
