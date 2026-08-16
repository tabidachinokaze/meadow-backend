package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.ChatMessageRepository
import moe.tabidachi.meadow.repository.ServerMemberRepository
import moe.tabidachi.meadow.repository.ServerRepository
import moe.tabidachi.meadow.repository.UserRepository

class ChatServiceImpl(
    private val chatMessageRepository: ChatMessageRepository,
    private val serverRepository: ServerRepository,
    private val serverMemberRepository: ServerMemberRepository,
    private val userRepository: UserRepository,
    private val chatHub: ChatHub,
) : ChatService {

    override suspend fun getHistory(
        serverId: Long,
        limit: Int,
        before: kotlin.time.Instant?,
    ): Response<ChatHistoryResult?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val messages = chatMessageRepository.getHistory(serverId, limit + 1, before)
        val hasMore = messages.size > limit
        val list = (if (hasMore) messages.dropLast(1) else messages).map { it.toInfo(null) }
        return CommonStatusCode.SUCCESS.withData(ChatHistoryResult(messages = list, hasMore = hasMore))
    }

    override suspend fun sendMessage(callerId: Long, serverId: Long, content: String): Response<ChatMessageInfo?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val user = userRepository.getByUid(callerId)
            ?: return UserStatusCode.USER_NOT_FOUND.emptyData()
        val message = chatMessageRepository.add(
            serverId = serverId,
            senderName = user.username,
            senderUuid = user.gameId,
            content = content,
            type = "chat",
        )
        val info = message.toInfo(null)
        // 实时广播给同服务器连接
        chatHub.broadcast(serverId, info)
        return CommonStatusCode.SUCCESS.withData(info)
    }

    override suspend fun recallMessage(callerId: Long, serverId: Long, messageId: Long): Response<Long?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (chatMessageRepository.getById(serverId, messageId) == null) {
            return ChatStatusCode.MESSAGE_NOT_FOUND.emptyData()
        }
        val isManager = serverMemberRepository.getByServerAndUser(serverId, callerId)?.role
            ?.let { it == ServerRole.OWNER || it == ServerRole.ADMIN } ?: false
        if (!isManager && !isSystemAdmin(callerId)) {
            return CommonStatusCode.FORBIDDEN.emptyData()
        }
        if (!chatMessageRepository.recall(serverId, messageId)) {
            return CommonStatusCode.FAILURE.emptyData()
        }
        return CommonStatusCode.SUCCESS.withData(messageId)
    }

    private suspend fun isSystemAdmin(userId: Long): Boolean =
        userRepository.getByUid(userId)?.role == moe.tabidachi.meadow.database.model.SystemRole.ADMIN

    private fun moe.tabidachi.meadow.database.model.ChatMessage.toInfo(senderRole: String?): ChatMessageInfo {
        return ChatMessageInfo(
            id = id,
            serverId = serverId,
            senderName = senderName,
            senderUuid = senderUuid,
            senderRole = senderRole,
            content = content,
            type = type,
            isBroadcast = isBroadcast,
            isRecalled = isRecalled,
            createdAt = createdAt,
        )
    }
}
