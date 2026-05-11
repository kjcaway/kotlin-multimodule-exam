package me.courtboard.api.api.board.dto

import java.time.LocalDateTime

data class BoardListResDto(
    val id: String,
    val title: String,
    val createdId: String,
    val createdName: String?,
    val createdAvatarUrl: String? = null,
    val createdAt: LocalDateTime,
    val excerpt: String? = null,
    val thumbnailUrl: String? = null,
    val commentCount: Long = 0,
)
