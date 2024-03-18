package me.multimoduleexam.domain

import jakarta.persistence.*

@Entity
@Table(name = "tbl_member")
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String,
    val email: String,
    val age: Int
)
