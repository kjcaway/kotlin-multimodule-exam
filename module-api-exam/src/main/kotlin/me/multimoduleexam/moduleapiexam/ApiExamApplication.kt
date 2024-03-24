package me.multimoduleexam.moduleapiexam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiExamApplication

fun main(args: Array<String>) {
    runApplication<ApiExamApplication>(*args)
}