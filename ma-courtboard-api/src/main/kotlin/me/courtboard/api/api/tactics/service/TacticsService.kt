package me.courtboard.api.api.tactics.service

import me.courtboard.api.api.error.dto.CustomRuntimeException
import me.courtboard.api.api.tactics.dto.TacticsReqDto
import me.courtboard.api.api.tactics.dto.TacticsResDto
import me.courtboard.api.api.tactics.dto.TacticsResDto.Companion.toTacticsResDto
import me.courtboard.api.api.tactics.repository.TacticsRepository
import me.multimoduleexam.util.JsonUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class TacticsService(
    private val tacticsRepository: TacticsRepository
) {

    fun createTactic(dto: TacticsReqDto): Map<String, Any> {
        val tacticsEntity = dto.toEntity()
        tacticsEntity.states = JsonUtil.convertToJsonStr(mapOf("formations" to dto.formations))
        tacticsEntity.createdId = "UNKNOWN"

        val entity = tacticsRepository.save(tacticsEntity)

        return mapOf(
            "id" to entity.id,
        )
    }

    fun getTactic(id: String): TacticsResDto {
        val entity = tacticsRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        return entity.toTacticsResDto()
    }
}