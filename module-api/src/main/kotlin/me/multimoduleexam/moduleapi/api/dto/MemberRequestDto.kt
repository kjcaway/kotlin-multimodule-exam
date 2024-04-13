package me.multimoduleexam.moduleapi.api.dto

import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import me.multimoduleexam.domain.Member

data class MemberRequestDto(
    val id: Long?,
    @field:NotBlank(message = "name cannot be blank")
    val name: String,
    @field:Email(message = "email is invalid")
    val email: String,
    @field:Digits(integer = 3 ,fraction = 0)
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