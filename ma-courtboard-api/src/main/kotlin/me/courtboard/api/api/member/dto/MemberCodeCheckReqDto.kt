package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class MemberCodeCheckReqDto(
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:Size(min = 6, max = 6, message = "code must not exceed 6 characters")
    val code: String
)