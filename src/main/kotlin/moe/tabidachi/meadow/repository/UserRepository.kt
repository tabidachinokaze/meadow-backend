package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.UserEntity
import moe.tabidachi.meadow.database.model.SystemRole
import moe.tabidachi.meadow.database.model.User
import moe.tabidachi.meadow.database.table.UserTable
import moe.tabidachi.meadow.ktx.setIfNotNull
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.UserMapper
import moe.tabidachi.meadow.model.UserInfo
import moe.tabidachi.meadow.security.Encryptor
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Instant

interface UserRepository {
    suspend fun getByUid(uid: Long): User?
    suspend fun getByEmail(email: String): User?
    suspend fun getByPhone(phone: String): User?
    suspend fun getByUsername(username: String): User?
    suspend fun getByGameId(gameId: String): User?
    suspend fun create(username: String, email: String, password: String/*, gameId: String*/): Long
    suspend fun getUserInfo(uid: Long): UserInfo?
    suspend fun updateLastLogin(uid: Long, time: Instant = Clock.System.now()): Instant?
    suspend fun updateUserInfo(
        callingUserId: Long,
        username: String? = null,
        email: String? = null,
        phone: String? = null,
        avatarUrl: String? = null,
        gameId: String? = null,
        bio: String? = null,
        website: String? = null,
        location: String? = null,
    ): Boolean

    suspend fun updatePassword(uid: Long, password: String): Boolean
}

class UserRepositoryImpl(
    private val database: Database,
    private val encryptor: Encryptor
) : UserRepository {
    override suspend fun getByUid(uid: Long): User? = database.withTransaction {
        UserEntity.find { UserTable.id.eq(uid) }
            .singleOrNull()
            ?.let(UserMapper::toUser)
    }

    override suspend fun getByEmail(email: String): User? = database.withTransaction {
        UserEntity.find { UserTable.email.eq(email) }
            .singleOrNull()
            ?.let(UserMapper::toUser)
    }

    override suspend fun getByPhone(phone: String): User? = database.withTransaction {
        UserEntity.find { UserTable.phone.eq(phone) }
            .singleOrNull()
            ?.let(UserMapper::toUser)
    }

    override suspend fun getByUsername(username: String): User? = database.withTransaction {
        UserEntity.find { UserTable.username.eq(username) }
            .singleOrNull()
            ?.let(UserMapper::toUser)
    }

    override suspend fun getByGameId(gameId: String): User? = database.withTransaction {
        UserEntity.find { UserTable.gameId.eq(gameId) }
            .singleOrNull()
            ?.let(UserMapper::toUser)
    }

    override suspend fun create(username: String, email: String, password: String/*, gameId: String*/): Long =
        database.withTransaction {
            val entity = UserEntity.new {
                this.username = username
                this.email = email
                this.password = encryptor.encrypt(password)
                //this.gameId = gameId
                val now = Clock.System.now()
                this.lastLogin = now
                this.createdAt = now
                this.updatedAt = now
            }
            entity.id.value
        }

    override suspend fun getUserInfo(uid: Long): UserInfo? = database.withTransaction {
        UserEntity.find { UserTable.id.eq(uid) }
            .singleOrNull()
            ?.let(UserMapper::toUserInfo)
    }

    override suspend fun updateLastLogin(uid: Long, time: Instant): Instant? = database.withTransaction {
        val updateCount = UserTable.update({ UserTable.id.eq(uid) }) {
            it[lastLogin] = time
        }
        if (updateCount > 0) time else null
    }

    override suspend fun updateUserInfo(
        callingUserId: Long,
        username: String?,
        email: String?,
        phone: String?,
        avatarUrl: String?,
        gameId: String?,
        bio: String?,
        website: String?,
        location: String?,
    ): Boolean = database.withTransaction {
        val updateCount = UserTable.update({ UserTable.id.eq(callingUserId) }) { statement ->
            statement.setIfNotNull(UserTable.username, username)
            statement.setIfNotNull(UserTable.email, email)
            statement.setIfNotNull(UserTable.phone, phone)
            statement.setIfNotNull(UserTable.avatarUrl, avatarUrl)
            statement.setIfNotNull(UserTable.gameId, gameId)
            statement.setIfNotNull(UserTable.bio, bio)
            statement.setIfNotNull(UserTable.website, website)
            statement.setIfNotNull(UserTable.location, location)
            statement[UserTable.updatedAt] = Clock.System.now()
        }
        updateCount > 0
    }

    override suspend fun updatePassword(uid: Long, password: String): Boolean = database.withTransaction {
        val updateCount = UserTable.update({ UserTable.id.eq(uid) }) {
            it[UserTable.password] = encryptor.encrypt(password)
            it[UserTable.updatedAt] = Clock.System.now()
        }
        updateCount > 0
    }
}
