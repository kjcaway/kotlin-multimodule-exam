package me.multimoduleexam.moduleapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["me.multimoduleexam"])
@EnableJpaRepositories(basePackages = ["me.multimoduleexam"])
@EntityScan(basePackages = ["me.multimoduleexam"])
class ModuleApiApplication

fun main(args: Array<String>) {
    runApplication<ModuleApiApplication>(*args)
}
