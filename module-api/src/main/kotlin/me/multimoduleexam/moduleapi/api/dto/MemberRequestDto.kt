package me.multimoduleexam.moduleapi.api.dto

import jakarta.validation.constraints.Email
import me.multimoduleexam.domain.Member

data class MemberRequestDto(
    val id: Long?,
    val name: String,
    @field:Email(message = "이메일 형식이 아닙니다.")
    val email: String,
    val age: Int
) {
    companion object {
        fun toEntity(dto: MemberRequestDto): Member {
            return Member(
                id = dto.id,
                name = dto.name,
                email = dto.email,
                age = dto.age
            )
        }
    }
}