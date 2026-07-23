package me.courtboard.api.api.quicktactics.repository

import me.courtboard.api.api.quicktactics.entity.QuickTacticsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuickTacticsRepository : JpaRepository<QuickTacticsEntity, String>
