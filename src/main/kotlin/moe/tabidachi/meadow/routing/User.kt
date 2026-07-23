package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.requireUserId
import moe.tabidachi.meadow.service.UserService

fun Route.user() {
    val userService: UserService by application.dependencies

    get("/users/{uid}") {
        val uid = call.parameters["uid"]!!.toLong()
        val from = requireUserId()
        call.respond(userService.getUserInfo(uid, from == uid))
    }
}