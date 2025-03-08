package me.multimoduleexam.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NodeRepository : JpaRepository<Node, Long> {
    fun findAllByType(type: String): List<Node>

    @Query(
        value = """
        WITH RECURSIVE node_hierarchy AS (
            -- Base case: Start with a specific node (e.g., a user node with id=1)
            SELECT
                n.id,
                n.name,
                n.type,
                0 AS level,
                CAST(n.name AS CHAR(1000)) AS path
            FROM
                tbl_node n
            WHERE
                n.id = :id
        
            UNION ALL
        
            -- Recursive case: Find all nodes connected to the current node
            SELECT
                n.id,
                n.name,
                n.type,
                nh.level + 1,
                CONCAT(nh.path, ' -> ', n.name) AS path
            FROM
                node_hierarchy nh
                    JOIN
                tbl_edge e ON nh.id = e.src_node_id
                    JOIN
                tbl_node n ON e.dst_node_id = n.id
            WHERE
                nh.level < 10  -- Prevents infinite recursion, adjust as needed
        )
        
        -- Query the CTE results
        SELECT
            id,
            name,
            type,
            level,
            path
        FROM
            node_hierarchy
        ORDER BY
            level, name;
    """, nativeQuery = true
    )
    fun recursiveFindById(id: Long): List<*>
}