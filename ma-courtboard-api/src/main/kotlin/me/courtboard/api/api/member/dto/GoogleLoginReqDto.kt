package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.NotBlank

data class GoogleLoginReqDto(
    @field:NotBlank(message = "credential is required")
    val credential: String
)
