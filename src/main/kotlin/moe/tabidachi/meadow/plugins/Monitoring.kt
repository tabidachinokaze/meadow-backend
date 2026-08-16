package moe.tabidachi.meadow.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(DoubleReceive)
    install(CallLogging) {
        level = Level.ERROR
        filter { call -> call.request.path().startsWith("/") }
        if (false) format { call ->
            runBlocking {
                buildString {
                    appendLine()
                    appendLine("<-------------------")
                    appendLine(call.request.httpMethod)
                    appendLine(call.request.uri)
                    appendLine("header:")
                    appendLine(
                        call.request.headers.entries().joinToString("\n", postfix = "\n") { "${it.key}: ${it.value}" })
                    appendLine("attributes:")
                    appendLine(call.attributes.allKeys.joinToString("\n", postfix = "\n"))
                    appendLine("------------------->")
                    appendLine(
                        call.response.headers.allValues().entries()
                            .joinToString("\n", postfix = "\n") { "${it.key}: ${it.value}" })
                    appendLine("processingTimeMillis: ${call.processingTimeMillis()}")
                    appendLine("response status: ${call.response.status()}")
                }
            }
        }
    }
    val bodyLoggerPlugin = createApplicationPlugin(name = "BodyLoggerPlugin") {
        // Intercept incoming requests
        onCall { call ->
            // WebSocket 升级请求无 body，读取会挂起并干扰握手，跳过
            val isWebSocketUpgrade = call.request.headers[HttpHeaders.Upgrade]
                ?.equals("websocket", ignoreCase = true) == true
            if (!isWebSocketUpgrade) {
                val requestBody = call.receiveText()
                call.application.environment.log.info("Request Body: $requestBody")
            }
        }

        // Intercept outgoing responses
        onCallRespond { call, body ->
            // Ensure the body is readable text metadata, not a raw channel or file stream
            call.application.environment.log.info("Response Body: $body")
        }
    }

    install(bodyLoggerPlugin)
}
