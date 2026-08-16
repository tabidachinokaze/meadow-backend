package moe.tabidachi.meadow.service

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.HeadBucketRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.ktor.http.*
import moe.tabidachi.meadow.model.config.S3Config

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
        val objectKey = "${avatarBucket}/$fileName"

        val request = PutObjectRequest {
            bucket = avatarBucket
            key = objectKey
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return buildUrl(objectKey)
    }

    override suspend fun uploadScreenshot(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String {
        ensureBucket(screenshotBucket)
        val objectKey = "${screenshotBucket}/$fileName"

        val request = PutObjectRequest {
            bucket = screenshotBucket
            key = objectKey
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return buildUrl(objectKey)
    }

    override suspend fun uploadFile(
        bucketName: String,
        bytes: ByteArray,
        fileName: String,
        contentType: String,
    ): String {
        ensureBucket(bucketName)
        val objectKey = "$bucketName/$fileName"

        val request = PutObjectRequest {
            bucket = bucketName
            key = objectKey
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return buildUrl(objectKey)
    }

    private fun buildUrl(objectKey: String): String {
        return URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = s3Config.host
            path(objectKey)
        }.buildString()
    }
}