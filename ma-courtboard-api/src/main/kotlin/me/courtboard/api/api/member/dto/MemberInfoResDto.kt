package me.courtboard.api.api.member.dto

import me.courtboard.api.api.member.entity.MemberInfoEntity
import java.time.LocalDateTime

data class MemberInfoResDto(
    val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String? = null,
    val createdAt: LocalDateTime,
    val lastloginAt: LocalDateTime? = null
) {
    companion object {
        fun MemberInfoEntity.toMemberInfoResDto(): MemberInfoResDto {
            return MemberInfoResDto(
                id = this.id.toString(),
                name = this.name!!,
                email = this.email!!,
                avatarUrl = this.avatarUrl,
                createdAt = this.createdAt,
                lastloginAt = this.lastloginAt
            )
        }
    }
}