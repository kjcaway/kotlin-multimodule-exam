package me.multimoduleexam.modulebatch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@SpringBootApplication(scanBasePackages = ["me.multimoduleexam"])
@EnableJpaRepositories(basePackages = ["me.multimoduleexam"])
@EntityScan(basePackages = ["me.multimoduleexam"])
class BatchApplication

fun main(args: Array<String>) {
    val springApplication =
        SpringApplicationBuilder(BatchApplication::class.java).web(WebApplicationType.NONE).build()
    springApplication.run(*args)
}
