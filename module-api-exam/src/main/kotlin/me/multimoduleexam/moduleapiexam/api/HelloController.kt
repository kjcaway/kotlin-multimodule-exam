package me.multimoduleexam.moduleapiexam.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/hello")
class HelloController {

    @GetMapping
    fun get(): ResponseEntity<*> {
        return ResponseEntity.ok("hello world!")
    }
}