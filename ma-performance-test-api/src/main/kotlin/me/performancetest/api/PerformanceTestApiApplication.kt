package me.performancetest.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["me.multimoduleexam"])
@EntityScan(basePackages = ["me.multimoduleexam"])
class PerformanceTestApiApplication

fun main(args: Array<String>) {
    runApplication<PerformanceTestApiApplication>(*args)
}