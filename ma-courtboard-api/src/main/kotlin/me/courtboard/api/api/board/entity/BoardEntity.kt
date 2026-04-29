package me.courtboard.api.api.board.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "tbl_board")
data class BoardEntity(
    @Id
    val id: String,

    @Column(nullable = false, length = 256)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var contents: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_id")
    var createdId: String = "UNKNOWN",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_id")
    var updatedId: String? = null,
)
