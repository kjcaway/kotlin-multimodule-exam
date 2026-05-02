package me.courtboard.api.api.casbin.dto

import jakarta.validation.constraints.NotBlank

data class PolicyAddReqDto(
    @field:NotBlank
    val sub: String,
    @field:NotBlank
    val obj: String,
    @field:NotBlank
    val act: String,
)
