package me.multimouleexam.moduleapi.api

import me.multimouleexam.util.DateUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping
    fun get():ResponseEntity<*> {
        val now = DateUtil.getNowDateStr()
        return ResponseEntity.ok(now)
    }
}