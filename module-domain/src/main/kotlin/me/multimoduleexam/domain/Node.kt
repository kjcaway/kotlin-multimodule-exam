package me.multimoduleexam.domain

import jakarta.persistence.*

@Entity
@Table(name = "tbl_node")
data class Node (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    val type: String,
    val name: String,
)