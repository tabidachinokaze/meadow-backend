package moe.tabidachi.meadow.service

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.HeadBucketRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray
import io.ktor.http.*
import moe.tabidachi.meadow.model.config.S3Config
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface StorageService {
    suspend fun uploadAvatar(
        bytes: ByteArray,
        fileName: String,
        contentType: String = ContentType.Image.PNG.toString()
    ): String

    /** 上传截图（bucket: screenshots，公开读） */
    suspend fun uploadScreenshot(
        bytes: ByteArray,
        fileName: String,
        contentType: String = ContentType.Image.PNG.toString()
    ): String

    /** 通用文件上传（bucket 由调用方指定） */
    suspend fun uploadFile(
        bucket: String,
        bytes: ByteArray,
        fileName: String,
        contentType: String,
    ): String

    /** 生成对象预签名 GET URL（私有桶临时访问，默认 15 分钟） */
    suspend fun presignedUrl(bucket: String, key: String, expiresIn: Duration = 15.minutes): String

    /** 从存储 URL（bucket/key 路径式）生成预签名 URL */
    suspend fun presignObjectUrl(objectUrl: String, expiresIn: Duration = 15.minutes): String

    /** 读取对象字节（后端代理下载/图片，避免私有桶直连与混合内容） */
    suspend fun downloadObject(bucket: String, key: String): ByteArray

    /** 从存储 URL 提取 bucket 与 key（形如 http://host:port/{bucket}/{key}） */
    fun splitObjectUrl(objectUrl: String): Pair<String, String>?
}

class S3Service(
    private val s3Config: S3Config,
    private val s3Client: S3Client,
    private val avatarBucket: String = "avatar",
    private val screenshotBucket: String = "screenshots",
) : StorageService {
    /** 已确认存在的 bucket（避免每次请求都 head） */
    private val ensuredBuckets = mutableSetOf<String>()

    private suspend fun ensureBucket(bucketName: String) {
        if (bucketName in ensuredBuckets) return
        try {
            s3Client.headBucket(HeadBucketRequest { this.bucket = bucketName })
        } catch (e: Exception) {
            // 自建 MinIO/S3 兼容场景 bucket 可能未预建，自动创建
            s3Client.createBucket(CreateBucketRequest { this.bucket = bucketName })
        }
        ensuredBuckets += bucketName
    }

    override suspend fun uploadAvatar(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String {
        ensureBucket(avatarBucket)

        val request = PutObjectRequest {
            bucket = avatarBucket
            key = fileName
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return buildUrl(avatarBucket, fileName)
    }

    override suspend fun uploadScreenshot(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String {
        ensureBucket(screenshotBucket)

        val request = PutObjectRequest {
            bucket = screenshotBucket
            key = fileName
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return buildUrl(screenshotBucket, fileName)
    }

    override suspend fun uploadFile(
        bucketName: String,
        bytes: ByteArray,
        fileName: String,
        contentType: String,
    ): String {
        ensureBucket(bucketName)

        val request = PutObjectRequest {
            bucket = bucketName
            key = fileName
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return buildUrl(bucketName, fileName)
    }

    private fun buildUrl(bucketName: String, objectKey: String): String {
        return URLBuilder().apply {
            // 与 SharedS3Client endpoint 一致：HTTP + 配置端口（自建 S3 兼容服务）
            protocol = URLProtocol.HTTP
            host = s3Config.host
            port = s3Config.port
            path(bucketName, objectKey)
        }.buildString()
    }

    override suspend fun presignedUrl(bucket: String, key: String, expiresIn: Duration): String {
        val bucketName = bucket
        val objectKey = key
        val request = GetObjectRequest {
            this.bucket = bucketName
            this.key = objectKey
        }
        val signed = s3Client.presignGetObject(request, expiresIn)
        // 预签名 URL 直接可用（forcePathStyle 下已含 bucket 路径与签名参数）
        return signed.url.toString()
    }

    override suspend fun presignObjectUrl(objectUrl: String, expiresIn: Duration): String {
        // 存储 URL 形如 http://host:port/{bucket}/{key}
        val path = objectUrl.substringAfter("://").substringAfter("/") // bucket/key...
        val parts = path.split("/")
        if (parts.size < 2) return objectUrl
        val bucketName = parts[0]
        val key = parts.drop(1).joinToString("/")
        return presignedUrl(bucketName, key, expiresIn)
    }

    override suspend fun downloadObject(bucket: String, key: String): ByteArray {
        val bucketName = bucket
        val objectKey = key
        val request = GetObjectRequest {
            this.bucket = bucketName
            this.key = objectKey
        }
        // getObject 为泛型 transform API：在 transform 中消费 body 流
        return s3Client.getObject(request) { response ->
            response.body!!.toByteArray()
        }
    }

    override fun splitObjectUrl(objectUrl: String): Pair<String, String>? {
        val path = objectUrl.substringAfter("://").substringAfter("/")
        val parts = path.split("/")
        if (parts.size < 2) return null
        return parts[0] to parts.drop(1).joinToString("/")
    }
}