package me.multimoduleexam.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EdgeRepository : JpaRepository<Edge, EdgeId> {
}