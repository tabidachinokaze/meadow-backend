package moe.tabidachi.meadow.jwt

object Claims {
    const val UID = "uid"
    /** token 版本号：与数据库 token_version 比对，防止旧 token 在改密/注销后继续有效 */
    const val TOKEN_VERSION = "tv"
    /** 用户创建时间（epoch 秒）：删库重建后新用户 uid 可能复用，但 createdAt 不同 → 旧 token 立即失效 */
    const val CREATED_AT = "ca"
}