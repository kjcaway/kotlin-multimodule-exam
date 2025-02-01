package me.courtboard.api.api.tactics.dto

import me.courtboard.api.api.tactics.entity.TacticsEntity
import me.multimoduleexam.util.GeneratorUtil

data class TacticsReqDto(
    val title: String,
    val description: String?,
    val formations: Map<String, Formation>
) {
    data class Formation(
        val players: List<Player>,
        val ball: Ball
    )

    data class Player(
        val id: Long,
        val x: Int,
        val y: Int,
        val color: String,
        val name: String,
        val visible: Boolean?
    )

    data class Ball(
        val x: Int,
        val y: Int,
        val visible: Boolean?
    )

    fun toEntity(): TacticsEntity {
        return TacticsEntity(
            id = GeneratorUtil.generateUUIDWithoutDashes(),
            name = this.title,
            description = this.description,
        )
    }
}