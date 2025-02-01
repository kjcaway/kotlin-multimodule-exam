package me.courtboard.api.api.tactics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

import java.time.LocalDateTime


@Entity
@Table(name = "tbl_tactics")
data class TacticsEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column
    var states: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_id")
    var createdId: String = "UNKNOWN_ID",

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_id")
    val updatedId: String? = null,
)