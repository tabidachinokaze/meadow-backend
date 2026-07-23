package moe.tabidachi.meadow.model

enum class RelationType {
    FRIEND,
    FOLLOW,
}

enum class RelationStatus {
    PENDING,
    ACTIVE,
    BLOCKED,
    REJECTED,
    REMOVE
}
