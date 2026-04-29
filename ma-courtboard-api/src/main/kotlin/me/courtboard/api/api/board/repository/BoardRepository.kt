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
       SELECT b.id, b.title, b.created_id, b.created_at, mi.name as created_name
       FROM tbl_board b
       LEFT JOIN tbl_memberinfo mi ON cast(mi.id as text) = b.created_id
       ORDER BY b.created_at DESC
       OFFSET :start ROWS FETCH NEXT :limit ROWS ONLY
    """,
        nativeQuery = true,
    )
    fun findAllForList(@Param("start") start: Int, @Param("limit") limit: Int): List<Array<Any>>
}
