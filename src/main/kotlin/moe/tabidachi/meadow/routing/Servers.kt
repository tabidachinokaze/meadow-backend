package moe.tabidachi.meadow.routing

import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.request.GameIdBindRequest
import moe.tabidachi.meadow.model.request.ServerInitializeRequest
import moe.tabidachi.meadow.model.request.ServerRegisterRequest
import moe.tabidachi.meadow.model.request.ServerUpdateRequest
import moe.tabidachi.meadow.service.ServerService

fun Route.servers() {
    val serverService: ServerService by application.dependencies
    get("/bind-code") {
        val gameId = call.queryParameters["name"] ?: throw MissingRequestParameterException("name", "query")
        val response = serverService.bindRequest(callingUserId, gameId)
        call.respond(response)
    }
    route("/servers") {
        get {
            val response = serverService.getServers()
            call.respond(response)
        }
        post<ServerRegisterRequest> { request ->
            val response = serverService.register(
                ownerId = callingUserId,
                request = request
            )
            call.respond(response)
        }
        route("/{id}") {
            get {
                val serverId = getParameter<Long>("id")
                val response = serverService.getServerById(serverId, callingUserId)
                call.respond(response)
            }
            put<ServerUpdateRequest> { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.update(
                    ownerId = callingUserId,
                    serverId = serverId,
                    request = request
                )
                call.respond(response)
            }
            delete {
                val serverId = getParameter<Long>("id")
                val response = serverService.delete(
                    ownerId = callingUserId,
                    serverId = serverId
                )
                call.respond(response)
            }
            authenticate(AuthenticationNames.NONE) {
                post<GameIdBindRequest>("/bind") { request ->
                    val serverId = getParameter<Long>("id")
                    val response = serverService.bindConfirm(serverId, request)
                    call.respond(response)
                }
                post<ServerInitializeRequest>("/init") { request ->
                    val serverId = getParameter<Long>("id")
                    val response = serverService.initialize(serverId, request)
                    call.respond(response)
                }
            }
        }
    }
}
