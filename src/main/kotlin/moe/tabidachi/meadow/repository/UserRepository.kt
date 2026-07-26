package moe.tabidachi.meadow.repository


import moe.tabidachi.meadow.database.entity.UserEntity
import moe.tabidachi.meadow.database.model.User
import moe.tabidachi.meadow.database.table.UserTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.UserMapper
import moe.tabidachi.meadow.model.UserInfo
import moe.tabidachi.meadow.security.Encryptor
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Instant

interface UserRepository {
    suspend fun getByEmail(email: String): User?
    suspend fun getByUsername(username: String): User?
    suspend fun getByGameId(gameId: String): User?
    suspend fun create(username: String, email: String, password: String, gameId: String): Long
    suspend fun getUserInfo(uid: Long): UserInfo?
    suspend fun updateLastLogin(uid: Long, time: Instant = Clock.System.now()): Instant?
}

class UserRepositoryImpl(
    private val database: Database,
    private val encryptor: Encryptor
) : UserRepository {
    override suspend fun getByEmail(email: String): User? = database.withTransaction {
        UserEntity.find { UserTable.email.eq(email) }
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

    override suspend fun create(username: String, email: String, password: String, gameId: String): Long =
        database.withTransaction {
            val entity = UserEntity.new {
                this.username = username
                this.email = email
                this.password = encryptor.hash(password.toCharArray())
                this.gameId = gameId
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
}
