package moe.tabidachi.meadow.routing

import io.ktor.http.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import moe.tabidachi.meadow.ktx.requireUserId
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.UserStatusCode
import moe.tabidachi.meadow.service.UserService

fun Route.user() {
    val userService: UserService by application.dependencies

    get("/users/{uid}") {
        val uid = call.parameters["uid"]!!.toLong()
        val from = requireUserId()
        call.respond(userService.getUserInfo(uid, from == uid))
    }.describe {
        summary = "获取用户信息"
        parameters {
            path("uid") {
                description = "用户ID"
                required = true
            }
        }
        responses {
            HttpStatusCode.OK {
                description = buildString {
                    appendLine("code:")
                    listOf(
                        UserStatusCode.USER_NOT_FOUND,
                        CommonStatusCode.SUCCESS
                    ).forEach {
                        appendLine("- ${it.code}: ${it.message}")
                    }
                    appendLine()
                    appendLine("data: token")
                }
            }
        }
    }
}