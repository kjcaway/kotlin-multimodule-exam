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
        checkDtoDetail(dto)

        val createdBy = CourtboardContext.getContext().memberId
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
            throw CustomRuntimeException(HttpStatus.FORBIDDEN)
        }
        val memberId = CourtboardContext.getContext().memberId
        val entityList = tacticsRepository.findAllByCreatedIdOrderByCreatedAtDesc(memberId)

        return entityList.map { it.toTacticsListResDto() }
    }

    fun updateTactic(id: String, dto: TacticsReqDto): Map<String, Any> {
        checkOwner(id)
        checkDtoDetail(dto)

        val entity = tacticsRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        entity.name = dto.title
        entity.description = dto.description
        entity.states = JsonUtil.convertToJsonStr(
            mapOf(
                "formations" to dto.formations,
                "playerInfo" to dto.playerInfo
            )
        )

        val updatedEntity = tacticsRepository.save(entity)

        return mapOf(
            "id" to updatedEntity.id,
        )
    }

    fun deleteTactic(id: String) {
        checkOwner(id)

        val entity = tacticsRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        tacticsRepository.delete(entity)
    }

    fun checkOwner(id: String) {
        val memberId = CourtboardContext.getContext().memberId
        val entity = tacticsRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        if(entity.createdId == "UNKNOWN") {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "this tactic is not created by you")
        }

        if (entity.createdId != memberId) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        }
    }

    fun checkDtoDetail(dto: TacticsReqDto) {
        if (dto.hasAllSameBallPosition()) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "each ball formations cannot be equals")
        }

        if (dto.hasAllSamePlayerPosition()) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "each player formations cannot be equals")
        }
    }
}