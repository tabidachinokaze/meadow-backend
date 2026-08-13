package moe.tabidachi.meadow.service

import aws.sdk.kotlin.services.s3.S3Client
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
}

class S3Service(
    private val s3Config: S3Config,
    private val s3Client: S3Client,
    private val avatarBucket: String = "avatar",
) : StorageService {
    override suspend fun uploadAvatar(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String {
        val objectKey = "${avatarBucket}/$fileName"

        val request = PutObjectRequest {
            bucket = avatarBucket
            key = objectKey
            this.contentType = contentType
            acl = aws.sdk.kotlin.services.s3.model.ObjectCannedAcl.PublicRead
            body = ByteStream.fromBytes(bytes)
        }

        s3Client.putObject(request)

        return URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = s3Config.host
            path(objectKey)
        }.buildString()
    }
}