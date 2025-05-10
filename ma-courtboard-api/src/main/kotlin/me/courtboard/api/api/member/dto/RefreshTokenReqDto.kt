package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Size

data class RefreshTokenReqDto(
    @field:Size(min = 16, max = 256, message = "invalid refresh token")
    val refreshToken: String
)