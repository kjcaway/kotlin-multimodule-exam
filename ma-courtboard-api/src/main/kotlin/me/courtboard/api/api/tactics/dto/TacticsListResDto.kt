package me.courtboard.api.api.tactics.dto

import me.courtboard.api.api.tactics.entity.TacticsEntity
import java.time.LocalDateTime

data class TacticsListResDto(
    val id: String,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val isTemplate: Boolean? = null,
    val createdAt: LocalDateTime,
    val createdName: String? = null,
) {

    companion object {
        fun TacticsEntity.toTacticsListResDto(): TacticsListResDto {
            return TacticsListResDto(
                id = this.id,
                name = this.name,
                description = this.description,
                isPublic = this.isPublic,
                isTemplate = this.isTemplate,
                createdAt = this.createdAt,
            )
        }
    }
}