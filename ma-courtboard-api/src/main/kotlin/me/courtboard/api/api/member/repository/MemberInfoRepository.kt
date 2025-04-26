package me.courtboard.api.api.member.repository

import me.courtboard.api.api.member.entity.MemberInfoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberInfoRepository: JpaRepository<MemberInfoEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): MemberInfoEntity?
}