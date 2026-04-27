package me.courtboard.api.api.member.dto

import me.courtboard.api.api.member.entity.MemberInfoEntity
import java.time.LocalDateTime

data class MemberAdminListResDto(
    val id: String,
    val email: String,
    val name: String?,
    val provider: String?,
    val createdAt: LocalDateTime,
    val lastloginAt: LocalDateTime?
) {
    companion object {
        fun MemberInfoEntity.toMemberAdminListResDto(): MemberAdminListResDto {
            return MemberAdminListResDto(
                id = this.id.toString(),
                email = this.email ?: "",
                name = this.name,
                provider = this.provider,
                createdAt = this.createdAt,
                lastloginAt = this.lastloginAt
            )
        }
    }
}
