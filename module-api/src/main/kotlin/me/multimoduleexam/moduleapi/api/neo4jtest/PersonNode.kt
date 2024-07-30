package me.multimoduleexam.moduleapi.api.neo4jtest

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

@Node("Person")
data class PersonNode(
    @Id
    val id: String?,
    val name: String,
    val age: Int?,

    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    var likesList: MutableList<LikesRelationship>?
)
