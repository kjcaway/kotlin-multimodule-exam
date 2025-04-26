package me.courtboard.api.api.member.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "tbl_members")
data class MemberEntity(
    @Id
    val id: UUID,

    @Column(nullable = false)
    var passwd: String
)