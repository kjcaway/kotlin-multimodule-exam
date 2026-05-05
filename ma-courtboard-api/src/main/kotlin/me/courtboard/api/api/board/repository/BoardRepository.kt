package me.courtboard.api.api.board.repository

import me.courtboard.api.api.board.entity.BoardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BoardRepository : JpaRepository<BoardEntity, String> {

    @Query(
        """
       SELECT b.id, b.title, b.created_id, b.created_at, mi.name as created_name, b.contents, mi.avatar_url as created_avatar_url
       FROM tbl_board b
       LEFT JOIN tbl_memberinfo mi ON cast(mi.id as text) = b.created_id
       ORDER BY b.created_at DESC
       OFFSET :start ROWS FETCH NEXT :limit ROWS ONLY
    """,
        nativeQuery = true,
    )
    fun findAllForList(@Param("start") start: Int, @Param("limit") limit: Int): List<Array<Any?>>

    @Query(
        """
       SELECT b.* FROM tbl_board b
       JOIN tbl_memberinfo mi ON cast(mi.id as text) = b.created_id
       WHERE mi.name = :name AND b.title = :title
       ORDER BY b.created_at DESC
       LIMIT 1
    """,
        nativeQuery = true,
    )
    fun findFirstByAuthorNameAndTitle(
        @Param("name") name: String,
        @Param("title") title: String,
    ): BoardEntity?
}
