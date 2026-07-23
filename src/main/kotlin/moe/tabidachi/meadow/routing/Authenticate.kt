package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import moe.tabidachi.meadow.model.request.LoginRequest
import moe.tabidachi.meadow.model.request.SignupRequest
import moe.tabidachi.meadow.service.AuthService

fun Route.authenticate() {
    val authService: AuthService by application.dependencies
    post<SignupRequest>("/signup") { request ->
        call.respond(authService.signup(request))
    }
    post<LoginRequest>("/login") { request ->
        call.respond(authService.login(request))
    }
}
