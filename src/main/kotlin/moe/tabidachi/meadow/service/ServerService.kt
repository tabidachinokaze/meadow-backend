package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.GameIdBindRequest
import moe.tabidachi.meadow.model.request.ServerInitializeRequest
import moe.tabidachi.meadow.model.request.ServerRegisterRequest
import moe.tabidachi.meadow.model.request.ServerUpdateRequest
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.security.CaptchaValidator

interface ServerService {
    suspend fun register(ownerId: Long, request: ServerRegisterRequest): Response<ServerInfo?>
    suspend fun getServers(): Response<List<ServerInfo>>
    suspend fun getServerById(serverId: Long, callingUserId: Long): Response<ServerInfo?>
    suspend fun update(ownerId: Long, serverId: Long, request: ServerUpdateRequest): Response<ServerInfo?>
    suspend fun delete(ownerId: Long, serverId: Long): Response<Long?>
    suspend fun bindRequest(callingUserId: Long, gameId: String): Response<String?>
    suspend fun bindConfirm(serverId: Long, request: GameIdBindRequest): Response<String?>
    suspend fun initialize(serverId: Long, request: ServerInitializeRequest): Response<String?>
    suspend fun publishPublicKey(serverId: Long): Response<String?>
}

class ServerServiceImpl(
    private val serverRepository: ServerRepository,
    private val userRepository: UserRepository,
    private val captchaValidator: CaptchaValidator
) : ServerService {
    private val bindingUsers: MutableMap<String, Long> = mutableMapOf()

    override suspend fun register(ownerId: Long, request: ServerRegisterRequest): Response<ServerInfo?> {
        return when {
            serverRepository.getByAddress(
                host = request.host,
                port = request.port
            ) != null -> ServerStatusCode.SERVER_ALREADY_EXISTS.emptyData()

            else -> {
                val serverId = serverRepository.create(
                    name = request.name,
                    description = request.description,
                    host = request.host,
                    port = request.port,
                    modLoader = request.modLoader,
                    version = request.version,
                    bannerUrl = request.bannerUrl,
                    tags = request.tags,
                    ownerId = ownerId,
                    rconHost = request.rconHost,
                    rconPort = request.rconPort,
                    rconPassword = request.rconPassword,
                    serverKey = request.serverKey,
                    machineId = request.machineId,
                )
                val serverInfo = serverRepository.getServerInfo(serverId)
                CommonStatusCode.SUCCESS.withData(serverInfo)
            }
        }
    }

    override suspend fun getServers(): Response<List<ServerInfo>> {
        return CommonStatusCode.SUCCESS.withData(serverRepository.getServers().map { it.desensitize() })
    }

    override suspend fun getServerById(serverId: Long, callingUserId: Long): Response<ServerInfo?> {
        val serverInfo = serverRepository.getServerInfo(serverId)
        return if (serverInfo == null) {
            ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        } else {
            val isOwner = serverInfo.ownerId == callingUserId
            CommonStatusCode.SUCCESS.withData(if (isOwner) serverInfo else serverInfo.desensitize())
        }
    }

    override suspend fun update(ownerId: Long, serverId: Long, request: ServerUpdateRequest): Response<ServerInfo?> {
        val server = serverRepository.getById(serverId)
        return when {
            server == null -> ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
            server.ownerId != ownerId -> CommonStatusCode.FORBIDDEN.emptyData()
            request.isEmpty() -> ServerStatusCode.WITHOUT_ANY_FIELDS.emptyData()
            else -> {
                val updateResult = serverRepository.update(
                    serverId = serverId,
                    name = request.name,
                    description = request.description,
                    host = request.host,
                    port = request.port,
                    modLoader = request.modLoader,
                    version = request.version,
                    bannerUrl = request.bannerUrl,
                    tags = request.tags,
                    rconHost = request.rconHost,
                    rconPort = request.rconPort,
                    rconPassword = request.rconPassword
                )
                if (updateResult) {
                    CommonStatusCode.SUCCESS.withData(serverRepository.getServerInfo(serverId)!!)
                } else {
                    CommonStatusCode.FAILURE.emptyData()
                }
            }
        }
    }

    override suspend fun delete(
        ownerId: Long,
        serverId: Long
    ): Response<Long?> {
        val server = serverRepository.getById(serverId)
        return when {
            server == null -> ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
            server.ownerId != ownerId -> CommonStatusCode.FORBIDDEN.emptyData()
            else -> {
                if (serverRepository.delete(serverId)) {
                    CommonStatusCode.SUCCESS.withData(serverId)
                } else {
                    CommonStatusCode.FAILURE.emptyData()
                }
            }
        }
    }

    override suspend fun bindRequest(callingUserId: Long, gameId: String): Response<String?> {
        val callingUser = userRepository.getByUid(callingUserId)
        val userByGameId = userRepository.getByGameId(gameId)
        return when {
            callingUser == null -> UserStatusCode.USER_NOT_FOUND.emptyData()
            callingUser.gameId == gameId || userByGameId != null -> UserStatusCode.GAME_ID_EXISTS.emptyData()
            else -> {
                val code = captchaValidator.generate("bind:code:${callingUserId}:${gameId}")
                bindingUsers[gameId] = callingUserId
                CommonStatusCode.SUCCESS.withData(code)
            }
        }
    }

    override suspend fun bindConfirm(serverId: Long, request: GameIdBindRequest): Response<String?> {
        val server = serverRepository.getById(serverId)
        return when {
            server == null -> ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
            server.serverKey != request.serverKey -> ServerStatusCode.SERVER_KEY_ERROR.emptyData()
            server.machineId != request.machineId -> ServerStatusCode.ENVIRONMENT_CHANGED.emptyData()
            else -> {
                val bindingUser = bindingUsers[request.name] ?: return ServerStatusCode.BIND_FAILURE.emptyData()
                when (captchaValidator.validate("bind:code:${bindingUser}:${request.name}", request.code)) {
                    CaptchaValidator.ValidationResult.CORRECT -> {
                        if (userRepository.updateUserInfo(bindingUser, gameId = request.name)) {
                            CommonStatusCode.SUCCESS.emptyData()
                        } else {
                            CommonStatusCode.FAILURE.emptyData()
                        }
                    }

                    CaptchaValidator.ValidationResult.ERROR -> UserStatusCode.VERIFICATION_CODE_ERROR.emptyData()
                    CaptchaValidator.ValidationResult.EXPIRED -> UserStatusCode.VERIFICATION_CODE_EXPIRED.emptyData()
                }
            }
        }
    }

    override suspend fun initialize(
        serverId: Long,
        request: ServerInitializeRequest
    ): Response<String?> {
        val server = serverRepository.getById(serverId)
        return when {
            server == null -> ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
            else -> {
                if (server.serverKey == request.serverKey) {
                    val updateResult = serverRepository.update(
                        serverId = serverId,
                        machineId = request.machineId,
                        isVerified = true
                    )
                    if (updateResult) {
                        CommonStatusCode.SUCCESS.emptyData()
                    } else {
                        CommonStatusCode.FAILURE.emptyData()
                    }
                } else {
                    ServerStatusCode.SERVER_KEY_ERROR.emptyData()
                }
            }
        }
    }

    override suspend fun publishPublicKey(serverId: Long): Response<String?> {
        val server = serverRepository.getById(serverId)
        TODO("Not yet implemented")
    }
}