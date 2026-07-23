package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.ERROR
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            buildString {
                appendLine()
                appendLine("<-------------------")
                appendLine(call.request.httpMethod)
                appendLine(call.request.uri)
                appendLine("header:")
                appendLine(call.request.headers.entries().joinToString("\n", postfix = "\n") { "${it.key}: ${it.value}" })
                appendLine("attributes:")
                appendLine(call.attributes.allKeys.joinToString("\n", postfix = "\n"))
                appendLine("------------------->")
                appendLine(call.response.headers.allValues().entries().joinToString("\n", postfix = "\n") { "${it.key}: ${it.value}" })
                appendLine("processingTimeMillis: ${call.processingTimeMillis()}")
                appendLine("response status: ${call.response.status()}")
            }
        }
    }
}
