package moe.tabidachi.meadow.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.emptyData

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                CommonStatusCode.INTERNAL_SERVER_ERROR.emptyData(
                    message = cause.cause?.message ?: cause.message
                )
            )
        }
        status(HttpStatusCode.NotFound) {
            call.respond(CommonStatusCode.NOT_FOUND.emptyData())
        }
    }
}