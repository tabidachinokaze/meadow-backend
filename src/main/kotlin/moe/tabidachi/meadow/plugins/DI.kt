package moe.tabidachi.meadow.plugins

import aws.sdk.kotlin.services.s3.S3Client
import com.resend.Resend
import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.contract.Qualifier
import moe.tabidachi.meadow.database.table.BanTable
import moe.tabidachi.meadow.database.table.ChatMessageTable
import moe.tabidachi.meadow.database.table.FavoriteTable
import moe.tabidachi.meadow.database.table.MapConfigTable
import moe.tabidachi.meadow.database.table.ModTable
import moe.tabidachi.meadow.database.table.ModpackTable
import moe.tabidachi.meadow.database.table.PlayerPositionTable
import moe.tabidachi.meadow.database.table.ReportTable
import moe.tabidachi.meadow.database.table.ScreenshotTable
import moe.tabidachi.meadow.database.table.ServerMemberTable
import moe.tabidachi.meadow.database.table.ServerPlayerTable
import moe.tabidachi.meadow.database.table.WorldTable
import moe.tabidachi.meadow.database.table.ServerTable
import moe.tabidachi.meadow.database.table.UserRelationTable
import moe.tabidachi.meadow.database.table.UserTable
import moe.tabidachi.meadow.model.config.*
import moe.tabidachi.meadow.repository.*
import moe.tabidachi.meadow.security.*
import moe.tabidachi.meadow.service.*
import moe.tabidachi.meadow.shared.SharedS3Client
import moe.tabidachi.meadow.system.Postman
import moe.tabidachi.meadow.system.PostmanResendImpl
import moe.tabidachi.meadow.system.PostmanTestImpl
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Duration.Companion.seconds

fun Application.configureDI() {
    // development 标记：兼容布尔/字符串两种解析（yaml 支持 ${MEADOW_DEVELOPMENT:true} 环境变量覆盖）
    val devRaw = propertyOrNull<String>("ktor.development")?.toString() ?: "true"
    val testing = devRaw.equals("true", ignoreCase = true)
    val mode = if (testing) "test" else "main"
    val argon2Config = property<Argon2Config>("argon2")
    val jwtConfig = property<JwtConfig>("jwt")
    val s3Config = property<S3Config>("s3")
    val redisConfig = property<RedisConfig>("redis")
    val resendApiKey = property<String>("resend.api_key")
    val secretKey = property<String>("encryption.secret_key")
    dependencies {
        provide<Encryptor>(Qualifier.ARGON2_ENCRYPTOR) {
            Argon2Encryptor(
                iterations = argon2Config.iterations,
                memory = argon2Config.memory,
                parallelism = argon2Config.parallelism
            )
        }
        provide<Encryptor>(Qualifier.AES_ENCRYPTOR) {
            AesEncryptor(
                secretKey = secretKey
            )
        }
        provide<Encryptor>(Qualifier.RSA_ENCRYPTOR) {
            RsaEncryptor()
        }
        provide<Jwt> {
            JwtImpl(
                secret = jwtConfig.secret,
                issuer = jwtConfig.issuer,
                audience = arrayOf(jwtConfig.audience)
            )
        }
        provide<S3Client> {
            SharedS3Client(s3Config)
        }
        provide<StorageService> {
            S3Service(
                s3Config = s3Config,
                s3Client = resolve()
            )
        }
        provide<KedisClient> {
            KedisClient(
                configuration = KedisConfiguration(
                    endpoint = KedisConfiguration.Endpoint.HostPort(
                        host = redisConfig.host,
                        port = redisConfig.port
                    ),
                    authentication = if (redisConfig.password != null) {
                        KedisConfiguration.Authentication.AutoAuth(
                            password = redisConfig.password,
                            username = redisConfig.username,
                        )
                    } else {
                        KedisConfiguration.Authentication.NoAutoAuth
                    },
                    connectionTimeout = 1.seconds
                )
            )
        }
        provide<Resend> {
            Resend(resendApiKey)
        }
        provide<Postman> {
            if (testing) {
                PostmanTestImpl()
            } else {
                PostmanResendImpl(
                    resend = resolve()
                )
            }
        }
        provide<CaptchaValidator> {
            CaptchaValidatorRedisImpl(
                kedisClient = resolve()
            )
        }
    }
    val (url, user, driver, password) = property<DatabaseConfig>("database.$mode")
    // PostgreSQL 部署时若目标库不存在则自动创建（H2 文件库自动生成，跳过）
    if (mode == "main") {
        ensureDatabaseExists(url, user, password)
    }
    dependencies {
        provide<Database> {
            Database.connect(url, driver, user, password).also { db ->
                transaction(db) {
                    SchemaUtils.create(
                        UserTable, UserRelationTable, ServerTable, ServerMemberTable, FavoriteTable,
                        ScreenshotTable, ServerPlayerTable, ModTable, ChatMessageTable, WorldTable,
                        ModpackTable, ReportTable, BanTable, MapConfigTable, PlayerPositionTable
                    )
                }
            }
        }
    }
    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl(
                database = resolve(),
                encryptor = resolve(Qualifier.ARGON2_ENCRYPTOR)
            )
        }
        provide<UserRelationRepository> {
            UserRelationRepositoryImpl(
                database = resolve()
            )
        }
        provide<ServerRepository> {
            ServerRepositoryImpl(
                database = resolve(),
                encryptor = resolve(Qualifier.AES_ENCRYPTOR)
            )
        }
        provide<ServerMemberRepository> {
            ServerMemberRepositoryImpl(
                database = resolve()
            )
        }
        provide<FavoriteRepository> {
            FavoriteRepositoryImpl(
                database = resolve()
            )
        }
        provide<ScreenshotRepository> {
            ScreenshotRepositoryImpl(
                database = resolve()
            )
        }
        provide<ServerPlayerRepository> {
            ServerPlayerRepositoryImpl(
                database = resolve()
            )
        }
        provide<ModRepository> {
            ModRepositoryImpl(
                database = resolve()
            )
        }
        provide<ChatMessageRepository> {
            ChatMessageRepositoryImpl(
                database = resolve()
            )
        }
        provide<WorldRepository> {
            WorldRepositoryImpl(
                database = resolve()
            )
        }
        provide<ModpackRepository> {
            ModpackRepositoryImpl(
                database = resolve()
            )
        }
        provide<ReportRepository> {
            ReportRepositoryImpl(
                database = resolve()
            )
        }
        provide<BanRepository> {
            BanRepositoryImpl(
                database = resolve()
            )
        }
        provide<MapConfigRepository> {
            MapConfigRepositoryImpl(
                database = resolve()
            )
        }
        provide<PlayerPositionRepository> {
            PlayerPositionRepositoryImpl(
                database = resolve()
            )
        }
    }
    dependencies {
        provide<PermissionGuard> {
            PermissionGuardImpl(
                serverMemberRepository = resolve()
            )
        }
        provide<AuthService> {
            AuthServiceImpl(
                jwt = resolve(),
                encryptor = resolve(Qualifier.ARGON2_ENCRYPTOR),
                userRepository = resolve(),
                postman = resolve(),
                captchaValidator = resolve()
            )
        }
        provide<UserService> {
            UserServiceImpl(
                userRepository = resolve(),
                userRelationRepository = resolve(),
                encryptor = resolve(Qualifier.ARGON2_ENCRYPTOR),
                captchaValidator = resolve(),
                chatMessageRepository = resolve(),
                favoriteRepository = resolve(),
                screenshotRepository = resolve(),
                serverPlayerRepository = resolve()
            )
        }
        provide<ServerService> {
            ServerServiceImpl(
                serverRepository = resolve(),
                userRepository = resolve(),
                captchaValidator = resolve(),
                serverMemberRepository = resolve(),
                permissionGuard = resolve(),
                database = resolve()
            )
        }
        provide<ServerMemberService> {
            ServerMemberServiceImpl(
                serverMemberRepository = resolve(),
                serverRepository = resolve(),
                userRepository = resolve(),
                database = resolve()
            )
        }
        provide<FavoriteService> {
            FavoriteServiceImpl(
                favoriteRepository = resolve(),
                serverRepository = resolve()
            )
        }
        provide<ScreenshotService> {
            ScreenshotServiceImpl(
                screenshotRepository = resolve(),
                serverRepository = resolve(),
                serverMemberRepository = resolve(),
                userRepository = resolve(),
                storageService = resolve(),
                reportRepository = resolve(),
                database = resolve()
            )
        }
        provide<ServerStatusService> {
            ServerStatusServiceImpl(
                serverRepository = resolve(),
                serverPlayerRepository = resolve(),
                modRepository = resolve(),
                playerPositionRepository = resolve(),
                database = resolve()
            )
        }
        provide<PlayerService> {
            PlayerServiceImpl(
                serverPlayerRepository = resolve(),
                serverRepository = resolve()
            )
        }
        provide<ModService> {
            ModServiceImpl(
                modRepository = resolve(),
                serverRepository = resolve()
            )
        }
        provide<ChatHub> {
            ChatHub()
        }
        provide<ChatService> {
            ChatServiceImpl(
                chatMessageRepository = resolve(),
                serverRepository = resolve(),
                serverMemberRepository = resolve(),
                userRepository = resolve(),
                chatHub = resolve()
            )
        }
        provide<WorldService> {
            WorldServiceImpl(
                worldRepository = resolve(),
                serverRepository = resolve(),
                storageService = resolve(),
                permissionGuard = resolve()
            )
        }
        provide<ModpackService> {
            ModpackServiceImpl(
                modpackRepository = resolve(),
                serverRepository = resolve(),
                storageService = resolve(),
                permissionGuard = resolve(),
                database = resolve()
            )
        }
        provide<AdminService> {
            AdminServiceImpl(
                reportRepository = resolve(),
                banRepository = resolve(),
                screenshotRepository = resolve(),
                serverRepository = resolve(),
                serverMemberRepository = resolve(),
                userRepository = resolve(),
                permissionGuard = resolve(),
                database = resolve()
            )
        }
        provide<MapService> {
            MapServiceImpl(
                mapConfigRepository = resolve(),
                playerPositionRepository = resolve(),
                serverRepository = resolve(),
                userRepository = resolve(),
                permissionGuard = resolve()
            )
        }
    }
}

/**
 * PostgreSQL 部署时自动创建目标数据库（若不存在）。
 *
 * 背景：`CREATE DATABASE` 不能运行在事务内，且连不上不存在的库；
 * 因此先尝试直连目标库，失败且报"库不存在"时，改连同实例的 `postgres` 库执行建库。
 * 需注意：连接用户必须具有 CREATEDB 权限；并发启动时捕获 42P04 重复库错误忽略。
 * H2 文件库自动生成，不走此逻辑。
 */
private fun ensureDatabaseExists(url: String, user: String, password: String) {
    // 1. 直连目标库：成功则无需建库
    try {
        java.sql.DriverManager.getConnection(url, user, password).use { }
        return
    } catch (e: java.sql.SQLException) {
        // 仅当报"数据库不存在"时才继续建库；其余错误（认证/网络等）直接抛出便于定位
        if (!e.message.orEmpty().contains("does not exist", ignoreCase = true)) {
            throw e
        }
    }
    // 2. 连同实例的 postgres 库执行 CREATE DATABASE
    val dbName = url.substringAfterLast('/').substringBefore('?')
    val query = if (url.contains('?')) "?" + url.substringAfter('?') else ""
    val adminUrl = if (dbName.isNotBlank()) {
        url.substringBeforeLast('/') + "/postgres" + query
    } else {
        url
    }
    java.sql.DriverManager.getConnection(adminUrl, user, password).use { conn ->
        conn.createStatement().use { stmt ->
            try {
                stmt.execute("CREATE DATABASE \"$dbName\"")
            } catch (e: java.sql.SQLException) {
                // 42P04 = duplicate_database（并发启动时另一实例已创建）
                if (e.sqlState != "42P04") throw e
            }
        }
    }
}
