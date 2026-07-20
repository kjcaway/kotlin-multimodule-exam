package me.multimoduleexam.moduleapiexam.api

import me.multimoduleexam.moduleapiexam.global.dto.ApiResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/hello")
class HelloController (
    @Value("\${k8sConfig.app.datasourceUsername:}") val datasourceUsername: String,
) {

    @GetMapping
    fun get(): ResponseEntity<*> {
        return ResponseEntity.ok(ApiResult.ok("hello world!"))
    }

    @GetMapping("/check/k8sconfig")
    fun checkK8sConfig(): ResponseEntity<*> {
        return ResponseEntity.ok(ApiResult.ok(datasourceUsername))
    }
}
