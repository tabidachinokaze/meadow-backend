package moe.tabidachi.meadow.routing

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
import moe.tabidachi.meadow.service.ModpackService

/**
 * 整合包路由（规划 §9.8）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.modpacks() {
    val modpackService: ModpackService by application.dependencies

    route("/servers/{id}") {
        get("/modpack") {
            val serverId = getParameter<Long>("id")
            call.respond(modpackService.getModpack(serverId))
        }

        get("/modpack/download") {
            val serverId = getParameter<Long>("id")
            call.respond(modpackService.download(serverId))
        }

        post("/modpack") {
            val serverId = getParameter<Long>("id")
            var fileBytes: ByteArray? = null
            var contentType = "application/zip"
            var version: String? = null
            var releaseDate: String? = null
            var changelog: String? = null

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
                            "version" -> version = part.value
                            "release_date" -> releaseDate = part.value
                            "changelog" -> changelog = part.value
                        }
                    }

                    else -> {
                    }
                }
                part.release.invoke()
            }

            if (fileBytes != null && !version.isNullOrBlank() && !releaseDate.isNullOrBlank()) {
                call.respond(
                    modpackService.update(callingUserId, serverId, fileBytes, contentType, version!!, releaseDate!!, changelog)
                )
            } else {
                call.respond(CommonStatusCode.FAILURE.emptyData<String>())
            }
        }
    }
}
