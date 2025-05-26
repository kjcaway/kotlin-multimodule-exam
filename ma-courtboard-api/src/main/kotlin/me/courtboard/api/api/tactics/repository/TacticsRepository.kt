package me.courtboard.api.api.tactics.repository

import me.courtboard.api.api.tactics.entity.TacticsEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TacticsRepository: JpaRepository<TacticsEntity, String> {
    fun findAllByCreatedIdOrderByCreatedAtDesc(createdId: String): List<TacticsEntity>

    @Query("""
       SELECT t.id, t.name, t.description, t.created_at, mi.name as created_name 
       FROM tbl_tactics t
       LEFT JOIN tbl_memberinfo mi ON cast(mi.id as text) = t.created_id
       WHERE t.created_id != 'UNKNOWN' AND t.is_public = true
       ORDER BY t.created_at DESC
   """, nativeQuery = true)
    fun findAllByPublic(pageable: Pageable): Page<Array<Any>>
}

