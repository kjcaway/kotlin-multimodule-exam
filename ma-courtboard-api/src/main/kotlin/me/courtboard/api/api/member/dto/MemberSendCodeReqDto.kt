package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Email

data class MemberSendCodeReqDto(
    @field:Email(message = "Invalid email format")
    val email: String
)