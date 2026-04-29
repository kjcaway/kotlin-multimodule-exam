package me.courtboard.api.api.board.dto

import java.time.LocalDateTime

data class BoardListResDto(
    val id: String,
    val title: String,
    val createdId: String,
    val createdName: String?,
    val createdAt: LocalDateTime,
)
