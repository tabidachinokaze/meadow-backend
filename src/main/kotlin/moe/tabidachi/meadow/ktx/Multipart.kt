package moe.tabidachi.meadow.ktx

import io.ktor.http.content.PartData
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

/**
 * 流式读取 multipart 文件 part，边读边计数，超过 [maxBytes] 立即返回 null（防 OOM DoS）。
 *
 * 相比 `part.provider().toByteArray()`（先整块读入内存再校验大小），
 * 该方法在读取过程中即中止超限 part，内存占用受 [maxBytes] 约束。
 * 返回 null 表示超限或空文件；调用方通过 part.release 完成资源清理。
 */
suspend fun PartData.FileItem.readBytesWithLimit(maxBytes: Long): ByteArray? {
    val channel = provider()
    var total = 0L
    val out = java.io.ByteArrayOutputStream()
    try {
        while (true) {
            val remaining = maxBytes - total
            if (remaining <= 0) return null
            // 每次至多读剩余配额 +1 字节：若读满说明已超限
            val chunk = channel.readRemaining(remaining + 1) ?: break
            val bytes = chunk.readByteArray()
            if (bytes.isEmpty()) break
            total += bytes.size
            out.write(bytes, 0, bytes.size)
        }
        return if (out.size() == 0) null else out.toByteArray()
    } finally {
        out.close()
        // 超限时通道未读完：取消以中止剩余传输（正常 EOF 时取消是安全空操作）
        if (total > maxBytes) {
            runCatching { channel.cancel(java.nio.channels.ClosedChannelException()) }
        }
    }
}
