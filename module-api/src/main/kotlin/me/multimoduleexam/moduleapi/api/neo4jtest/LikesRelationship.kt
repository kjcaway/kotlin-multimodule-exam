package me.multimoduleexam.moduleapi.api.neo4jtest

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode

@RelationshipProperties
data class LikesRelationship(
    @Id
    @GeneratedValue
    val id: Long?,

    @TargetNode
    val person: PersonNode
)
