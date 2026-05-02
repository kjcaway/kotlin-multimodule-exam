package me.courtboard.api.api.tactics.dto

import jakarta.validation.constraints.NotBlank

data class TacticsTemplateToggleReqDto(
    @field:NotBlank
    val id: String
)
