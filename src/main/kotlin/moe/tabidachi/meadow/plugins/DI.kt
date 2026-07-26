package moe.tabidachi.meadow.plugins

import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import com.resend.Resend
import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.database.table.UserRelationTable
import moe.tabidachi.meadow.database.table.UserTable
import moe.tabidachi.meadow.model.config.*
import moe.tabidachi.meadow.repository.UserRelationRepository
import moe.tabidachi.meadow.repository.UserRelationRepositoryImpl
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.repository.UserRepositoryImpl
import moe.tabidachi.meadow.security.*
import moe.tabidachi.meadow.service.AuthService
import moe.tabidachi.meadow.service.AuthServiceImpl
import moe.tabidachi.meadow.service.UserService
import moe.tabidachi.meadow.service.UserServiceImpl
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
    dependencies {
        provide<Encryptor> {
            Argon2Encryptor(
                iterations = argon2Config.iterations,
                memory = argon2Config.memory,
                parallelism = argon2Config.parallelism
            )
        }
        provide<Jwt> {
            JwtImpl(
                secret = jwtConfig.secret,
                issuer = jwtConfig.issuer,
                audience = arrayOf(jwtConfig.audience)
            )
        }
        provide<S3Client> {
            S3Client {
                this.region = "us-east-1"
                this.endpointUrl = Url {
                    this.scheme = Scheme.HTTP
                    this.host = Host.Domain(s3Config.host)
                    this.port = s3Config.port
                }
                this.credentialsProvider = object : CredentialsProvider {
                    override suspend fun resolve(attributes: Attributes): Credentials {
                        return Credentials(
                            accessKeyId = s3Config.accessKey,
                            secretAccessKey = s3Config.secretKey
                        )
                    }
                }
            }
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
                    SchemaUtils.create(UserTable, UserRelationTable)
                }
            }
        }
    }
    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl(
                database = resolve(),
                encryptor = resolve()
            )
        }
        provide<UserRelationRepository> {
            UserRelationRepositoryImpl(
                database = resolve()
            )
        }
    }
    dependencies {
        provide<AuthService> {
            AuthServiceImpl(
                jwt = resolve(),
                encryptor = resolve(),
                userRepository = resolve(),
                postman = resolve(),
                captchaValidator = resolve()
            )
        }
        provide<UserService> {
            UserServiceImpl(
                userRepository = resolve(),
                userRelationRepository = resolve(),
            )
        }
    }
}
