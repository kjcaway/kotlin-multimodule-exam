package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.Size

data class ChangePasswordReqDto (
    @field:Size(min = 8, message = "code must not be less than 8 characters")
    val currentPassword: String,
    @field:Size(min = 8, message = "code must not be less than 8 characters")
    val newPassword: String,
)