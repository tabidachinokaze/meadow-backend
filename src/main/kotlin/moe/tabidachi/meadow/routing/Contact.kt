package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.requireUserId
import moe.tabidachi.meadow.service.UserService

fun Route.contact() {
    val userService: UserService by application.dependencies

    get("/contacts") {
        val userId = requireUserId()
        call.respond(userService.getContracts(userId))
    }
}