package me.multimoduleexam.moduleapi.api.dto

import me.multimoduleexam.domain.Member

data class MemberDto(
    val id: Long?,
    val name: String,
    val email: String,
    val age: Int
) {
    companion object {
        fun toEntity(dto: MemberDto): Member {
            return Member(
                id = dto.id,
                name = dto.name,
                email = dto.email,
                age = dto.age
            )
        }
    }
}