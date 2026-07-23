package me.courtboard.api.api.quicktactics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 퀵보드 - 사용자별 마지막 작전판 상태를 1행으로 보관한다.
 * (member_id 당 1행, upsert)
 */
@Entity
@Table(name = "tbl_quick_tactics")
data class QuickTacticsEntity(
    @Id
    @Column(name = "member_id")
    val memberId: String,

    @Column
    var states: String,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
