package me.courtboard.api.api.member.repository

import me.courtboard.api.api.member.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemberRepository: JpaRepository<MemberEntity, UUID> {
}