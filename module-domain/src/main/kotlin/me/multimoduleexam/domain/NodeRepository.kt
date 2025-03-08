package me.multimoduleexam.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeRepository : JpaRepository<Node, Long> {
    fun findAllByType(type: String): List<Node>
}