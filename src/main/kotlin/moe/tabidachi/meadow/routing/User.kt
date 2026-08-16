package moe.tabidachi.meadow.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.callingUserIdOrNull
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.CommonStatusCode
import moe.tabidachi.meadow.model.UserInfo
import moe.tabidachi.meadow.model.UserStatusCode
import moe.tabidachi.meadow.model.ValidStatusCode
import moe.tabidachi.meadow.model.emptyData
import moe.tabidachi.meadow.model.request.RebindEmailRequest
import moe.tabidachi.meadow.model.request.UpdatePasswordRequest
import moe.tabidachi.meadow.model.request.UpdateUserInfoRequest
import moe.tabidachi.meadow.service.StorageService
import moe.tabidachi.meadow.service.UserService
import kotlin.uuid.Uuid

fun Route.user() {
    val userService: UserService by application.dependencies

    authenticate(AuthenticationNames.NONE) {
        get("/users/{uid}") {
            val targetUserId = getParameter<Long>("uid")
            call.respond(userService.getUserInfo(callingUserIdOrNull, targetUserId))
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
                        appendLine("data: 用户信息")
                    }
                }
            }
        }
    }

    post<UpdateUserInfoRequest>("/users/{uid}/update") { request ->
        val targetUserId = getParameter<Long>("uid")
        call.respond(userService.updateUserInfo(callingUserId, targetUserId, request))
    }.describe {
        summary = "更新用户信息"
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
                        CommonStatusCode.SUCCESS,
                        CommonStatusCode.FAILURE,
                        CommonStatusCode.FORBIDDEN
                    ).forEach {
                        appendLine("- ${it.code}: ${it.message}")
                    }
                    appendLine()
                    appendLine("data: 用户信息")
                }
            }
        }
    }

    post("/users/{uid}/avatar") {
        val targetUserId = getParameter<Long>("uid")
        val storageService = call.application.dependencies.resolve<StorageService>()
        val multipart = call.receiveMultipart()
        var uploadedUrl: String? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val fileBytes = part.provider().toByteArray()

                    if (fileBytes.isNotEmpty() && fileBytes.size <= 2 * 1024 * 1000) {
                        val uniqueFileName = "${callingUserId}_${Uuid.random().toHexString()}.png"
                        val mimeType = part.contentType?.toString() ?: ContentType.Image.PNG.toString()

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
            val response = userService.updateUserInfo(
                callingUserId,
                targetUserId,
                UpdateUserInfoRequest(avatarUrl = uploadedUrl)
            )

            call.respond(response)
        } else {
            call.respond(CommonStatusCode.FAILURE.emptyData<UserInfo>())
        }
    }.describe {
        summary = "更新用户头像"
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
                        CommonStatusCode.SUCCESS,
                        CommonStatusCode.FAILURE,
                        CommonStatusCode.FORBIDDEN
                    ).forEach {
                        appendLine("- ${it.code}: ${it.message}")
                    }
                }
            }
        }
    }

    post<RebindEmailRequest>("/users/{uid}/email") { request ->
        val targetUserId = getParameter<Long>("uid")
        call.respond(userService.updateEmail(callingUserId, targetUserId, request))
    }.describe {
        summary = "换绑邮箱"
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
                        CommonStatusCode.FORBIDDEN,
                        ValidStatusCode.INVALID_EMAIL,
                        UserStatusCode.EMAIL_ALREADY_EXISTS,
                        UserStatusCode.VERIFICATION_CODE_ERROR,
                        UserStatusCode.VERIFICATION_CODE_EXPIRED,
                        CommonStatusCode.SUCCESS
                    ).forEach {
                        appendLine("- ${it.code}: ${it.message}")
                    }
                    appendLine()
                    appendLine("data: 用户信息")
                }
            }
        }
    }

    post<UpdatePasswordRequest>("/users/{uid}/password") { request ->
        val targetUserId = getParameter<Long>("uid")
        call.respond(userService.updatePassword(callingUserId, targetUserId, request))
    }.describe {
        summary = "更新密码"
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
                        CommonStatusCode.SUCCESS,
                        UserStatusCode.PASSWORD_INCORRECT,
                        CommonStatusCode.FORBIDDEN
                    ).forEach {
                        appendLine("- ${it.code}: ${it.message}")
                    }
                }
            }
        }
    }

    post("/users/{uid}/deactivate") {
        val targetUserId = getParameter<Long>("uid")
        call.respond(userService.deactivateAccount(callingUserId, targetUserId))
    }.describe {
        summary = "注销账号（软删除）"
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
                        CommonStatusCode.SUCCESS,
                        CommonStatusCode.FORBIDDEN
                    ).forEach {
                        appendLine("- ${it.code}: ${it.message}")
                    }
                    appendLine()
                    appendLine("data: 被注销的用户ID")
                }
            }
        }
    }
}