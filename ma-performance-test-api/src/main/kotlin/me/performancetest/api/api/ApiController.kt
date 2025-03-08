package me.performancetest.api.api

import me.multimoduleexam.domain.EdgeRepository
import me.multimoduleexam.domain.NodeRepository
import me.performancetest.api.api.common.dto.ApiResult
import org.apache.logging.log4j.core.util.Integers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("/test/{id}")
    fun test1(@PathVariable id: Long): ApiResult<*> {
        val nodeList = nodeRepository.recursiveFindById(id)
        return ApiResult.ok(nodeList)
    }
}