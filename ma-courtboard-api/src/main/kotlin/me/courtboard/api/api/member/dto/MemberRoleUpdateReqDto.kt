package me.courtboard.api.api.member.dto

import jakarta.validation.constraints.NotBlank

data class MemberRoleUpdateReqDto(
    @field:NotBlank
    val role: String,
)
