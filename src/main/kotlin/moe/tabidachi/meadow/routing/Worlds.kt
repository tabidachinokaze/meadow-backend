package moe.tabidachi.meadow.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.emptyData
import moe.tabidachi.meadow.service.WorldService

/**
 * 存档路由（规划 §9.7）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.worlds() {
    val worldService: WorldService by application.dependencies

    route("/servers/{id}") {
        get("/worlds") {
            val serverId = getParameter<Long>("id")
            call.respond(worldService.getWorlds(serverId))
        }

        post("/worlds") {
            val serverId = getParameter<Long>("id")
            var fileBytes: ByteArray? = null
            var contentType = "application/zip"
            var worldName: String? = null
            var worldType = "survival"

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val bytes = part.provider().toByteArray()
                        if (bytes.isNotEmpty()) {
                            fileBytes = bytes
                            contentType = part.contentType?.toString() ?: "application/zip"
                        }
                    }

                    is PartData.FormItem -> {
                        when (part.name) {
                            "world_name" -> worldName = part.value
                            "world_type" -> worldType = part.value
                        }
                    }

                    else -> {
                    }
                }
                part.release.invoke()
            }

            if (fileBytes != null && !worldName.isNullOrBlank()) {
                call.respond(
                    worldService.upload(callingUserId, serverId, fileBytes, contentType, worldName!!, worldType)
                )
            } else {
                call.respond(CommonStatusCode.FAILURE.emptyData<String>())
            }
        }

        route("/worlds/{wid}") {
            get("/download") {
                val serverId = getParameter<Long>("id")
                val worldId = getParameter<Long>("wid")
                call.respond(worldService.download(serverId, worldId))
            }

            patch("/set-current") {
                val serverId = getParameter<Long>("id")
                val worldId = getParameter<Long>("wid")
                call.respond(worldService.setCurrent(callingUserId, serverId, worldId))
            }

            delete {
                val serverId = getParameter<Long>("id")
                val worldId = getParameter<Long>("wid")
                call.respond(worldService.delete(callingUserId, serverId, worldId))
            }
        }
    }
}
