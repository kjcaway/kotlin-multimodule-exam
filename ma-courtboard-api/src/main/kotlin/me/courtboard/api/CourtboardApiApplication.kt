package me.courtboard.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CourtboardApiApplication

fun main(args: Array<String>) {
    runApplication<CourtboardApiApplication>(*args)
}