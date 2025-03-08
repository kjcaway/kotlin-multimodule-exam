package me.multimoduleexam.modulebatch.service

import me.multimoduleexam.domain.Edge
import me.multimoduleexam.domain.EdgeId
import me.multimoduleexam.domain.EdgeRepository
import me.multimoduleexam.domain.NodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class EdgeBatchService(
    private val edgeRepository: EdgeRepository,
    private val nodeRepository: NodeRepository
) {

    @Transactional
    fun insertRandomEdge(size: Int) {

        val userNodes = nodeRepository.findAllByType("user")
        val roleNodes = nodeRepository.findAllByType("role")
        val resourceNodes = nodeRepository.findAllByType("resource")

        val random = Random()
        val edges = mutableListOf<Edge>()

        repeat(size) {
            val roll = random.nextInt(2) // 0 or 1 to determine which type of edge to create

            when (roll) {
                0 -> { // user -> role
                    if (userNodes.isNotEmpty() && roleNodes.isNotEmpty()) {
                        val src = userNodes[random.nextInt(userNodes.size)]
                        val dst = roleNodes[random.nextInt(roleNodes.size)]
                        val edgeId = EdgeId(src.id!!, dst.id!!, "user_to_role")
                        edges.add(Edge(edgeId))
                    }
                }
                1 -> { // role -> resource
                    if (roleNodes.isNotEmpty() && resourceNodes.isNotEmpty()) {
                        val src = roleNodes[random.nextInt(roleNodes.size)]
                        val dst = resourceNodes[random.nextInt(resourceNodes.size)]
                        val edgeId = EdgeId(src.id!!, dst.id!!, "role_to_resource")
                        edges.add(Edge(edgeId))
                    }
                }
            }
        }

        edgeRepository.saveAll(edges)
    }
}