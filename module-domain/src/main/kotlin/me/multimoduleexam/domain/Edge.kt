package me.multimoduleexam.domain

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "tbl_edge")
data class Edge (
    @EmbeddedId
    val id: EdgeId,
)

@Embeddable
data class EdgeId(
    val srcNodeId: Long,
    val dstNodeId: Long,
    val type: String
) : Serializable