package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class MemberGrantReqDto(
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:Size(min = 4, max = 8, message = "role must not exceed 4 characters")
    val role: String
)