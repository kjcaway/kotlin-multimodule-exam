package me.courtboard.api.api.tactics.dto

import me.courtboard.api.api.tactics.entity.TacticsEntity
import me.multimoduleexam.util.JsonUtil
import java.time.LocalDateTime

data class TacticsResDto(
    val id: String,
    val name: String,
    val description: String?,
    val states: States,
    val isPublic: Boolean,
    val createdAt: LocalDateTime,
    var createdName: String? = null,
) {
    data class States(
        val formations: Map<String, Formation>,
        val playerInfo: List<PlayerInfo>
    )

    data class Formation(
        val ball: Ball,
        val players: List<Player>
    )

    data class Ball(
        val x: Int,
        val y: Int,
    )

    data class Player(
        val id: Long,
        val x: Int,
        val y: Int,
    )

    data class PlayerInfo(
        val id: Long,
        val name: String,
        val color: String,
    )

    companion object {
        fun TacticsEntity.toTacticsResDto(): TacticsResDto {
            return TacticsResDto(
                id = this.id,
                name = this.name,
                description = this.description,
                states = JsonUtil.convertToObject(this.states!!, States::class.java),
                createdAt = this.createdAt,
                isPublic = this.isPublic,
            )
        }
    }

    fun updateCreatedName(name: String?) {
        this.createdName = name
    }
}