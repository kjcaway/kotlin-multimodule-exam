package me.courtboard.api.api.tactics.service

import me.courtboard.api.api.tactics.dto.TacticsListResDto
import me.courtboard.api.api.tactics.dto.TacticsListResDto.Companion.toTacticsListResDto
import me.courtboard.api.api.tactics.dto.TacticsReqDto
import me.courtboard.api.api.tactics.dto.TacticsResDto
import me.courtboard.api.api.tactics.dto.TacticsResDto.Companion.toTacticsResDto
import me.courtboard.api.api.tactics.repository.TacticsRepository
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.JsonUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class TacticsService(
    private val tacticsRepository: TacticsRepository
) {

    fun createTactic(dto: TacticsReqDto): Map<String, Any> {
        val createdBy = CourtboardContext.getContext().memberId
        if (dto.hasAllSameBallPosition()) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "each ball formations cannot be equals")
        }

        if (dto.hasAllSamePlayerPosition()) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "each player formations cannot be equals")
        }

        val tacticsEntity = dto.toEntity()
        tacticsEntity.states = JsonUtil.convertToJsonStr(
            mapOf(
                "formations" to dto.formations,
                "playerInfo" to dto.playerInfo
            )
        )
        tacticsEntity.createdId = createdBy

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

    fun getMyTactics(): List<TacticsListResDto> {
        if (!CourtboardContext.isLogin()) {
            throw CustomRuntimeException(HttpStatus.UNAUTHORIZED)
        }
        val memberId = CourtboardContext.getContext().memberId
        val entityList = tacticsRepository.findAllByCreatedIdOrderByCreatedAtDesc(memberId)

        return entityList.map { it.toTacticsListResDto() }
    }
}