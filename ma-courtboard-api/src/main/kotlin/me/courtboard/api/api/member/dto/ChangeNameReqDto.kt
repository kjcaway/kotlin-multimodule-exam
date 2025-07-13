package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Size

data class ChangeNameReqDto(
    @field:Size(min = 3, max = 32, message = "name must not exceed 32 characters")
    val name: String,
)
