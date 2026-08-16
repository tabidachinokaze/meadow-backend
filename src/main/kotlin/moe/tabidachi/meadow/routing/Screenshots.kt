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
import moe.tabidachi.meadow.model.request.ReportScreenshotRequest
import moe.tabidachi.meadow.service.ScreenshotService

/**
 * 截图路由（规划 §9.6）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.screenshots() {
    val screenshotService: ScreenshotService by application.dependencies

    route("/servers/{id}") {
        get("/screenshots") {
            val serverId = getParameter<Long>("id")
            val uploaderId = call.request.queryParameters["uploader_id"]?.toLongOrNull()
            val status = call.request.queryParameters["status"]
            call.respond(screenshotService.getScreenshots(serverId, uploaderId, status))
        }

        post("/screenshots") {
            val serverId = getParameter<Long>("id")
            var fileBytes: ByteArray? = null
            var contentType = ContentType.Image.PNG.toString()
            var description: String? = null
            var coordinates: String? = null

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val bytes = part.provider().toByteArray()
                        if (bytes.isNotEmpty() && bytes.size <= 10 * 1024 * 1024) {
                            fileBytes = bytes
                            contentType = part.contentType?.toString() ?: ContentType.Image.PNG.toString()
                        }
                    }

                    is PartData.FormItem -> {
                        when (part.name) {
                            "description" -> description = part.value
                            "coordinates" -> coordinates = part.value
                        }
                    }

                    else -> {
                    }
                }
                part.release.invoke()
            }

            if (fileBytes != null) {
                call.respond(
                    screenshotService.upload(
                        callerId = callingUserId,
                        serverId = serverId,
                        bytes = fileBytes,
                        contentType = contentType,
                        description = description,
                        coordinates = coordinates,
                    )
                )
            } else {
                call.respond(CommonStatusCode.FAILURE.emptyData<String>())
            }
        }

        route("/screenshots/{sid}") {
            get("/download") {
                val serverId = getParameter<Long>("id")
                val screenshotId = getParameter<Long>("sid")
                call.respond(screenshotService.download(serverId, screenshotId))
            }

            post<ReportScreenshotRequest>("/report") { request ->
                val serverId = getParameter<Long>("id")
                val screenshotId = getParameter<Long>("sid")
                call.respond(screenshotService.report(callingUserId, serverId, screenshotId, request.reason))
            }

            delete {
                val serverId = getParameter<Long>("id")
                val screenshotId = getParameter<Long>("sid")
                call.respond(screenshotService.delete(callingUserId, serverId, screenshotId))
            }
        }
    }

    get("/users/me/screenshots") {
        call.respond(screenshotService.getMyScreenshots(callingUserId))
    }
}
