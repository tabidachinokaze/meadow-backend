package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.service.UserService

fun Route.contact() {
    val userService: UserService by application.dependencies

    get("/contacts") {
        call.respond(userService.getContracts(callingUserId))
    }
}