package me.performancetest.api.api

import me.multimoduleexam.domain.EdgeRepository
import me.multimoduleexam.domain.NodeRepository
import me.performancetest.api.api.common.dto.ApiResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController(
    val nodeRepository: NodeRepository,
    val edgeRepository: EdgeRepository
) {
    @GetMapping("/test")
    fun test(): ApiResult<*> {
        val nodeList = nodeRepository.findAllByType("user")
        return ApiResult.ok(nodeList)
    }
}