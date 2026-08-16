package moe.tabidachi.meadow.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
                // 全局统一 snake_case 序列化（已有 @SerialName 的字段显式优先）
                namingStrategy = JsonNamingStrategy.SnakeCase
            }
        )
    }
}