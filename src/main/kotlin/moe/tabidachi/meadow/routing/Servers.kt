package moe.tabidachi.meadow.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.readBytesWithLimit
import moe.tabidachi.meadow.ktx.callingUserIdOrNull
import moe.tabidachi.meadow.ktx.readBytesWithLimit
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.ktx.readBytesWithLimit
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.ServerInfo
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.model.emptyData
import moe.tabidachi.meadow.model.request.GameIdBindRequest
import moe.tabidachi.meadow.model.request.ServerInitializeRequest
import moe.tabidachi.meadow.model.request.ServerRegisterRequest
import moe.tabidachi.meadow.model.request.ServerUpdateRequest
import moe.tabidachi.meadow.service.ServerService
import moe.tabidachi.meadow.service.StorageService
import kotlin.uuid.Uuid

fun Route.servers() {
    val serverService: ServerService by application.dependencies
    val jwtConfig = application.property<JwtConfig>("jwt")
    route("/servers") {
        // ── 公开（可选认证）：列表 / 详情 ──
        // 匿名或无效 token 也可访问；带有效 token 时详情按 owner 返回完整字段，否则脱敏
        authenticate(jwtConfig.name, AuthenticationNames.NONE) {
            get {
                val response = serverService.getServers()
                call.respond(response)
            }
            get("/{id}") {
                val serverId = getParameter<Long>("id")
                val response = serverService.getServerById(serverId, callingUserIdOrNull)
                call.respond(response)
            }
        }

        // ── 需要登录：注册 / 更新 / 删除 / 绑定码 ──
        authenticate(jwtConfig.name) {
            post<ServerRegisterRequest> { request ->
                val response = serverService.register(
                    ownerId = callingUserId,
                    request = request
                )
                call.respond(response)
            }
            put<ServerUpdateRequest>("/{id}") { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.update(
                    ownerId = callingUserId,
                    serverId = serverId,
                    request = request
                )
                call.respond(response)
            }
            post("/{id}/banner") {
                val serverId = getParameter<Long>("id")
                val storageService = call.application.dependencies.resolve<StorageService>()
                val multipart = call.receiveMultipart()
                var uploadedUrl: String? = null

                // 图片 MIME 白名单：仅允许常见图片格式
                val allowedImageTypes = setOf(
                    ContentType.Image.PNG.toString(),
                    ContentType.Image.JPEG.toString(),
                    "image/webp",
                )

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val mimeType = part.contentType?.toString() ?: ContentType.Image.PNG.toString()
                            val fileBytes = part.readBytesWithLimit(5 * 1024 * 1024)

                            if (mimeType in allowedImageTypes && fileBytes != null && fileBytes.isNotEmpty() && fileBytes.size <= 5 * 1024 * 1000) {
                                val uniqueFileName = "s_${serverId}_${Uuid.random().toHexString()}.png"
                                uploadedUrl = storageService.uploadAvatar(
                                    bytes = fileBytes,
                                    fileName = uniqueFileName,
                                    contentType = mimeType
                                )
                            }
                        }

                        else -> {
                        }
                    }
                    part.release.invoke()
                }

                if (uploadedUrl != null) {
                    // 仅 owner 可更新；update 内部校验 owner 权限
                    val response = serverService.update(
                        ownerId = callingUserId,
                        serverId = serverId,
                        request = ServerUpdateRequest(bannerUrl = uploadedUrl)
                    )
                    call.respond(response)
                } else {
                    call.respond(CommonStatusCode.FAILURE.emptyData<ServerInfo>())
                }
            }
            delete("/{id}") {
                val serverId = getParameter<Long>("id")
                val response = serverService.delete(
                    ownerId = callingUserId,
                    serverId = serverId
                )
                call.respond(response)
            }
        }

        // ── Mod 端认证（无 JWT，走 server_key / bind-code）：bind / init ──
        authenticate(AuthenticationNames.NONE) {
            post<GameIdBindRequest>("/{id}/bind") { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.bindConfirm(serverId, request)
                call.respond(response)
            }
            post<ServerInitializeRequest>("/{id}/init") { request ->
                val serverId = getParameter<Long>("id")
                val response = serverService.initialize(serverId, request)
                call.respond(response)
            }
        }
    }

    // bind-code 需要登录（绑定码绑定当前用户与游戏 ID）
    authenticate(jwtConfig.name) {
        get("/bind-code") {
            val gameId = call.queryParameters["name"] ?: throw MissingRequestParameterException("name", "query")
            val response = serverService.bindRequest(callingUserId, gameId)
            call.respond(response)
        }
    }
}
