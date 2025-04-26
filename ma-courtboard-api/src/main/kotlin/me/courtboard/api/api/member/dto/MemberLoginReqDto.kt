package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class MemberLoginReqDto(
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:Size(min = 8, message = "code must not be less than 8 characters")
    val password: String
)