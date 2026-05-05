package me.courtboard.api.api.board.service

import me.courtboard.api.api.board.dto.BoardListResDto
import me.courtboard.api.api.board.dto.BoardReqDto
import me.courtboard.api.api.board.dto.BoardResDto
import me.courtboard.api.api.board.dto.BoardResDto.Companion.toBoardResDto
import me.courtboard.api.api.board.entity.BoardEntity
import me.courtboard.api.api.board.repository.BoardRepository
import me.courtboard.api.api.board.util.BoardHtmlSanitizer
import me.courtboard.api.api.member.repository.MemberInfoRepository
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.GeneratorUtil
import org.jsoup.Jsoup
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.util.*
import kotlin.jvm.optionals.getOrElse

private const val EXCERPT_MAX_LENGTH = 200

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val memberInfoRepository: MemberInfoRepository,
    private val boardImageService: BoardImageService,
) {

    @Transactional
    fun createBoard(dto: BoardReqDto): Map<String, Any> {
        val createdBy = CourtboardContext.getContext().memberId
        val sanitizedContents = BoardHtmlSanitizer.sanitize(dto.contents)

        val entity = BoardEntity(
            id = GeneratorUtil.generateUUIDWithoutDashes(),
            title = dto.title,
            contents = sanitizedContents,
            createdId = createdBy,
        )

        val saved = boardRepository.save(entity)
        boardImageService.linkToBoard(saved.id, sanitizedContents, createdBy)
        return mapOf("id" to saved.id)
    }

    fun getBoards(start: Int, limit: Int): List<BoardListResDto> {
        val rows = boardRepository.findAllForList(start, limit)
        return rows.map { row ->
            val contents = row[5] as String?
            val (excerpt, thumbnailUrl) = summarizeContents(contents)
            BoardListResDto(
                id = row[0] as String,
                title = row[1] as String,
                createdId = row[2] as String,
                createdAt = (row[3] as Timestamp).toLocalDateTime(),
                createdName = row[4] as String?,
                createdAvatarUrl = row[6] as String?,
                excerpt = excerpt,
                thumbnailUrl = thumbnailUrl,
            )
        }
    }

    private fun summarizeContents(html: String?): Pair<String?, String?> {
        if (html.isNullOrBlank()) return null to null
        val doc = Jsoup.parse(html)
        val text = doc.text().trim().takeIf { it.isNotEmpty() }
        val excerpt = text?.let { if (it.length > EXCERPT_MAX_LENGTH) it.substring(0, EXCERPT_MAX_LENGTH) + "…" else it }
        val thumbnail = doc.select("img").firstOrNull()?.attr("src")?.takeIf { it.isNotBlank() }
        return excerpt to thumbnail
    }

    fun getBoard(id: String): BoardResDto {
        val entity = boardRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        val result = entity.toBoardResDto()
        if (entity.createdId != "UNKNOWN") {
            val memberInfo = runCatching { UUID.fromString(entity.createdId) }
                .mapCatching { memberInfoRepository.findById(it).getOrElse { null } }
                .getOrElse { null }
            result.updateCreatedName(memberInfo?.name)
        }
        return result
    }

    fun getBoardByAuthor(name: String, title: String): BoardResDto {
        val entity = boardRepository.findFirstByAuthorNameAndTitle(name, title)
            ?: throw CustomRuntimeException(HttpStatus.NOT_FOUND)

        val result = entity.toBoardResDto()
        result.updateCreatedName(name)
        return result
    }

    @Transactional
    fun updateBoard(id: String, dto: BoardReqDto): Map<String, Any> {
        val entity = checkOwner(id)
        val memberId = CourtboardContext.getContext().memberId
        val sanitizedContents = BoardHtmlSanitizer.sanitize(dto.contents)

        boardImageService.cleanupRemovedImages(entity.id, sanitizedContents)

        entity.title = dto.title
        entity.contents = sanitizedContents
        entity.updatedAt = java.time.LocalDateTime.now()
        entity.updatedId = memberId

        val saved = boardRepository.save(entity)
        boardImageService.linkToBoard(saved.id, sanitizedContents, memberId)
        return mapOf("id" to saved.id)
    }

    @Transactional
    fun deleteBoard(id: String) {
        val entity = checkOwner(id)
        boardImageService.deleteAllByBoard(entity.id)
        boardRepository.delete(entity)
    }

    private fun checkOwner(id: String): BoardEntity {
        val entity = boardRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND) }

        if (entity.createdId == "UNKNOWN") {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        }

        val memberId = CourtboardContext.getContext().memberId
        if (entity.createdId != memberId) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        }

        return entity
    }
}
