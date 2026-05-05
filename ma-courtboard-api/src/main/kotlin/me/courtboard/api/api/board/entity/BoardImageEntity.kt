package me.courtboard.api.api.board.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "tbl_board_image",
    indexes = [
        Index(name = "idx_board_image_board_id", columnList = "board_id"),
        Index(name = "idx_board_image_created_at", columnList = "created_at"),
    ],
)
data class BoardImageEntity(
    @Id
    @Column(length = 64)
    val id: String,

    @Column(name = "board_id", length = 64)
    var boardId: String? = null,

    @Column(name = "file_path", nullable = false, length = 512)
    val filePath: String,

    @Column(name = "url_path", nullable = false, length = 256)
    val urlPath: String,

    @Column(name = "mime_type", nullable = false, length = 64)
    val mimeType: String,

    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_id", nullable = false, length = 64)
    val createdId: String,
)
