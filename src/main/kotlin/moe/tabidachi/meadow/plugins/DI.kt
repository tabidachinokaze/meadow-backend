package moe.tabidachi.meadow.plugins

import aws.sdk.kotlin.services.s3.S3Client
import com.resend.Resend
import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.contract.Qualifier
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
    val testing = propertyOrNull<Boolean>("ktor.development") == true
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
    dependencies {
        provide<Database> {
            Database.connect(url, driver, user, password).also { db ->
                transaction(db) {
                    SchemaUtils.create(UserTable, UserRelationTable, ServerTable)
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
    }
    dependencies {
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
            )
        }
        provide<ServerService> {
            ServerServiceImpl(
                serverRepository = resolve(),
                userRepository = resolve(),
                captchaValidator = resolve()
            )
        }
    }
}
