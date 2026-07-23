package me.courtboard.api.api.quicktactics.dto

import me.courtboard.api.api.quicktactics.entity.QuickTacticsEntity
import me.multimoduleexam.util.JsonUtil
import java.time.LocalDateTime

/**
 * 퀵보드 조회 응답 - states 는 파싱된 상태 객체로 내려준다.
 */
data class QuickTacticsResDto(
    val states: Map<String, Any>,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun QuickTacticsEntity.toQuickTacticsResDto(): QuickTacticsResDto {
            return QuickTacticsResDto(
                states = JsonUtil.convertToMap(this.states),
                updatedAt = this.updatedAt,
            )
        }
    }
}
