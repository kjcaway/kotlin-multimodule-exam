package me.courtboard.api.api.board.service

import me.courtboard.api.api.board.dto.BoardCommentReqDto
import me.courtboard.api.api.board.dto.BoardCommentResDto
import me.courtboard.api.api.board.entity.BoardCommentEntity
import me.courtboard.api.api.board.repository.BoardCommentRepository
import me.courtboard.api.api.board.repository.BoardRepository
import me.courtboard.api.global.CourtboardContext
import me.courtboard.api.global.error.CustomRuntimeException
import me.multimoduleexam.util.GeneratorUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
class BoardCommentService(
    private val boardCommentRepository: BoardCommentRepository,
    private val boardRepository: BoardRepository,
) {

    @Transactional
    fun createComment(boardId: String, dto: BoardCommentReqDto): Map<String, Any> {
        val createdBy = CourtboardContext.getContext().memberId

        boardRepository.findById(boardId)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "board not found") }

        if (dto.parentId != null) {
            val parent = boardCommentRepository.findById(dto.parentId)
                .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "parent comment not found") }

            if (parent.boardId != boardId) {
                throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "parent comment belongs to a different board")
            }
            if (parent.parentId != null) {
                throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Replies can only be one level deep")
            }
            if (parent.deletedAt != null) {
                throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Cannot reply to a deleted comment")
            }
        }

        val now = LocalDateTime.now()
        val entity = BoardCommentEntity(
            id = GeneratorUtil.generateUUIDWithoutDashes(),
            boardId = boardId,
            parentId = dto.parentId,
            contents = dto.contents,
            createdId = createdBy,
            createdAt = now,
            updatedAt = now,
        )
        val saved = boardCommentRepository.save(entity)
        return mapOf("id" to saved.id)
    }

    fun getCommentsByBoard(boardId: String): List<BoardCommentResDto> {
        val rows = boardCommentRepository.findAllByBoardIdWithAuthor(boardId)
        val all = rows.map { row ->
            BoardCommentResDto(
                id = row[0] as String,
                boardId = row[1] as String,
                parentId = row[2] as String?,
                contents = row[3] as String?,
                createdId = row[4] as String,
                createdAt = (row[5] as Timestamp).toLocalDateTime(),
                updatedAt = (row[6] as Timestamp).toLocalDateTime(),
                deleted = row[7] != null,
                createdName = row[8] as String?,
                createdAvatarUrl = row[9] as String?,
            )
        }

        val parents = all.filter { it.parentId == null }
        val repliesByParent = all.filter { it.parentId != null }
            .groupBy { it.parentId!! }

        val result = mutableListOf<BoardCommentResDto>()
        for (parent in parents) {
            val activeReplies = (repliesByParent[parent.id] ?: emptyList())
                .filter { !it.deleted }
                .map { it.maskIfDeleted() }

            if (parent.deleted && activeReplies.isEmpty()) {
                continue
            }

            val view = parent.maskIfDeleted()
            view.replies.addAll(activeReplies)
            result.add(view)
        }
        return result
    }

    @Transactional
    fun updateComment(id: String, dto: BoardCommentReqDto): Map<String, Any> {
        val entity = checkOwner(id)
        if (entity.deletedAt != null) {
            throw CustomRuntimeException(HttpStatus.BAD_REQUEST, "Cannot edit deleted comment")
        }
        entity.contents = dto.contents
        entity.updatedAt = LocalDateTime.now()
        val saved = boardCommentRepository.save(entity)
        return mapOf("id" to saved.id)
    }

    @Transactional
    fun deleteComment(id: String) {
        val entity = checkOwner(id)
        if (entity.deletedAt != null) return
        entity.deletedAt = LocalDateTime.now()
        boardCommentRepository.save(entity)
    }

    @Transactional
    fun deleteAllByBoard(boardId: String) {
        boardCommentRepository.deleteAllByBoardId(boardId)
    }

    private fun checkOwner(id: String): BoardCommentEntity {
        val entity = boardCommentRepository.findById(id)
            .orElseThrow { CustomRuntimeException(HttpStatus.NOT_FOUND, "comment not found") }

        val memberId = CourtboardContext.getContext().memberId
        if (entity.createdId != memberId) {
            throw CustomRuntimeException(HttpStatus.FORBIDDEN, "you don't have permission to access this resource")
        }
        return entity
    }

    private fun BoardCommentResDto.maskIfDeleted(): BoardCommentResDto {
        if (!this.deleted) return this
        return this.copy(contents = null)
    }
}
