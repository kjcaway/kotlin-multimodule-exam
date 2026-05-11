package me.courtboard.api.api.board.repository

import me.courtboard.api.api.board.entity.BoardCommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BoardCommentRepository : JpaRepository<BoardCommentEntity, String> {

    @Query(
        """
       SELECT c.id, c.board_id, c.parent_id, c.contents, c.created_id,
              c.created_at, c.updated_at, c.deleted_at,
              mi.name AS created_name, mi.avatar_url AS created_avatar_url
       FROM tbl_board_comment c
       LEFT JOIN tbl_memberinfo mi ON cast(mi.id as text) = c.created_id
       WHERE c.board_id = :boardId
       ORDER BY c.created_at ASC
    """,
        nativeQuery = true,
    )
    fun findAllByBoardIdWithAuthor(@Param("boardId") boardId: String): List<Array<Any?>>

    @Modifying
    @Query("DELETE FROM BoardCommentEntity c WHERE c.boardId = :boardId")
    fun deleteAllByBoardId(@Param("boardId") boardId: String): Int
}
