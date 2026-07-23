package me.courtboard.api.api.quicktactics.service

import me.courtboard.api.api.quicktactics.dto.QuickTacticsReqDto
import me.courtboard.api.api.quicktactics.dto.QuickTacticsResDto
import me.courtboard.api.api.quicktactics.dto.QuickTacticsResDto.Companion.toQuickTacticsResDto
import me.courtboard.api.api.quicktactics.entity.QuickTacticsEntity
import me.courtboard.api.api.quicktactics.repository.QuickTacticsRepository
import me.courtboard.api.global.CourtboardContext
import me.multimoduleexam.util.JsonUtil
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class QuickTacticsService(
    private val quickTacticsRepository: QuickTacticsRepository,
) {

    /**
     * 내 마지막 퀵보드 상태 조회. 저장된 적이 없으면 null 반환(프론트는 기본 상태 사용).
     */
    fun getMyQuickTactics(): QuickTacticsResDto? {
        val memberId = CourtboardContext.getContext().memberId
        return quickTacticsRepository.findById(memberId)
            .getOrNull()
            ?.toQuickTacticsResDto()
    }

    /**
     * 내 퀵보드 상태 저장(upsert). member_id 당 1행만 유지된다.
     */
    fun saveMyQuickTactics(dto: QuickTacticsReqDto): Map<String, Any> {
        val memberId = CourtboardContext.getContext().memberId
        val statesJson = JsonUtil.convertToJsonStr(
            mapOf(
                "players" to dto.players,
                "ball" to dto.ball,
                "playerInfo" to dto.playerInfo,
                "isHalfCourt" to dto.isHalfCourt,
            )
        )

        val entity = quickTacticsRepository.findById(memberId)
            .getOrNull()
            ?.apply {
                states = statesJson
                updatedAt = LocalDateTime.now()
            }
            ?: QuickTacticsEntity(memberId = memberId, states = statesJson)

        quickTacticsRepository.save(entity)

        return mapOf("memberId" to memberId)
    }
}
