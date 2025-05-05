package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import me.courtboard.api.api.member.entity.MemberInfoEntity
import java.util.*

data class MemberReqDto(
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:Size(min = 3, max = 32, message = "name must not exceed 32 characters")
    val name: String,
    val avatarUrl: String? = null,
    @field:Size(min = 8, message = "code must not be less than 8 characters")
    val passwd: String,
    @field:Size(min = 6, max = 6, message = "code must not exceed 6 characters")
    val code: String
) {
    fun toEntity(): MemberInfoEntity {
        return MemberInfoEntity(
            id = UUID.randomUUID(),
            email = this.email,
            name = this.name,
            avatarUrl = this.avatarUrl ?: ""
        )
    }
}