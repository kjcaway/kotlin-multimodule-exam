package me.courtboard.api.api.board.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "tbl_board_comment",
    indexes = [
        Index(name = "idx_board_comment_board_id", columnList = "board_id"),
        Index(name = "idx_board_comment_parent_id", columnList = "parent_id"),
        Index(name = "idx_board_comment_created_at", columnList = "created_at"),
    ],
)
data class BoardCommentEntity(
    @Id
    @Column(length = 64)
    val id: String,

    @Column(name = "board_id", nullable = false, length = 64)
    val boardId: String,

    @Column(name = "parent_id", length = 64)
    val parentId: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var contents: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_id", nullable = false, length = 64)
    val createdId: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
)
