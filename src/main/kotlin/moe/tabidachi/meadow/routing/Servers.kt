package moe.tabidachi.meadow.routing

import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.callingUserIdOrNull
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.model.request.GameIdBindRequest
import moe.tabidachi.meadow.model.request.ServerInitializeRequest
import moe.tabidachi.meadow.model.request.ServerRegisterRequest
import moe.tabidachi.meadow.model.request.ServerUpdateRequest
import moe.tabidachi.meadow.service.ServerService

fun Route.servers() {
    val serverService: ServerService by application.dependencies
    val jwtConfig = application.property<JwtConfig>("jwt")
    route("/servers") {
        // ── 公开（可选认证）：列表 / 详情 ──
        // 匿名或无效 token 也可访问；带有效 token 时详情按 owner 返回完整字段，否则脱敏
        authenticate(jwtConfig.name, AuthenticationNames.NONE) {
            get {
                val response = serverService.getServers()
                call.respond(response)
            }
            get("/{id}") {
                val serverId = getParameter<Long>("id")
                val response = serverService.getServerById(serverId, callingUserIdOrNull)
                call.respond(response)
            }
        }

        // ── 需要登录：注册 / 更新 / 删除 / 绑定码 ──
        authenticate(jwtConfig.name) {
            post<ServerRegisterRequest> { request ->
                val response = serverService.register(
                    ownerId = callingUserId,
                    request = request
                )
                call.respond(response)
            }
            put<ServerUpdateRequest>("/{id}") { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.update(
                    ownerId = callingUserId,
                    serverId = serverId,
                    request = request
                )
                call.respond(response)
            }
            delete("/{id}") {
                val serverId = getParameter<Long>("id")
                val response = serverService.delete(
                    ownerId = callingUserId,
                    serverId = serverId
                )
                call.respond(response)
            }
        }

        // ── Mod 端认证（无 JWT，走 server_key / bind-code）：bind / init ──
        authenticate(AuthenticationNames.NONE) {
            post<GameIdBindRequest>("/{id}/bind") { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.bindConfirm(serverId, request)
                call.respond(response)
            }
            post<ServerInitializeRequest>("/{id}/init") { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.initialize(serverId, request)
                call.respond(response)
            }
        }
    }

    // bind-code 需要登录（绑定码绑定当前用户与游戏 ID）
    authenticate(jwtConfig.name) {
        get("/bind-code") {
            val gameId = call.queryParameters["name"] ?: throw MissingRequestParameterException("name", "query")
            val response = serverService.bindRequest(callingUserId, gameId)
            call.respond(response)
        }
    }
}
