package me.courtboard.api.api.board.dto

import me.courtboard.api.api.board.entity.BoardEntity
import java.time.LocalDateTime

data class BoardResDto(
    val id: String,
    val title: String,
    val contents: String?,
    val createdId: String,
    var createdName: String? = null,
    var createdAvatarUrl: String? = null,
    val createdAt: LocalDateTime,
) {

    fun updateCreatedName(name: String?) {
        this.createdName = name
    }

    fun updateCreatedAvatarUrl(url: String?) {
        this.createdAvatarUrl = url
    }

    companion object {
        fun BoardEntity.toBoardResDto(): BoardResDto {
            return BoardResDto(
                id = this.id,
                title = this.title,
                contents = this.contents,
                createdId = this.createdId,
                createdAt = this.createdAt,
            )
        }
    }
}
