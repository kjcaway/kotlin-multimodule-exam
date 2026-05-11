package me.courtboard.api.api.board.dto

import java.time.LocalDateTime

data class BoardCommentResDto(
    val id: String,
    val boardId: String,
    val parentId: String?,
    val contents: String?,
    val createdId: String,
    var createdName: String? = null,
    var createdAvatarUrl: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deleted: Boolean = false,
    val replies: MutableList<BoardCommentResDto> = mutableListOf(),
)
