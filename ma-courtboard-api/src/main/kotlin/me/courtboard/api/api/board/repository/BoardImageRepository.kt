package me.courtboard.api.api.board.repository

import me.courtboard.api.api.board.entity.BoardImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BoardImageRepository : JpaRepository<BoardImageEntity, String> {

    fun findAllByUrlPathIn(urlPaths: Collection<String>): List<BoardImageEntity>

    fun findAllByBoardId(boardId: String): List<BoardImageEntity>

    @Query(
        """
        SELECT i FROM BoardImageEntity i
        WHERE i.boardId IS NULL AND i.createdAt < :threshold
        """,
    )
    fun findOrphans(@Param("threshold") threshold: LocalDateTime): List<BoardImageEntity>
}
