package me.courtboard.api.api.tactics.repository

import me.courtboard.api.api.tactics.entity.TacticsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TacticsRepository: JpaRepository<TacticsEntity, String> {
}