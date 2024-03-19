package me.multimoduleexam.modulebatch

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@SpringBootApplication(scanBasePackages = ["me.multimoduleexam"])
@EnableJpaRepositories(basePackages = ["me.multimoduleexam"])
@EntityScan(basePackages = ["me.multimoduleexam"])
@EnableBatchProcessing
class BatchApplication

fun getJobName(args: Array<String>): String? {
    for (arg in args) {
        if (arg.startsWith("--job.name")) {
            return arg.split("=")[1]
        }
    }
    return null
}

fun getJobParameter(): JobParameters {
    val simpleDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val now = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).format(simpleDateFormat)
    return JobParametersBuilder().addString("execute.now", now).toJobParameters()
}

fun main(args: Array<String>) {
    val app = SpringApplicationBuilder(BatchApplication::class.java).web(WebApplicationType.NONE)
    val context = app.run(*args)

    try {
        val jobLauncher = context.getBean("jobLauncher") as JobLauncher
        val jobName: String = getJobName(args) ?: throw Exception("invalid job name")
        val job: Job = context.getBean(jobName) as Job
        val execution = jobLauncher.run(job, getJobParameter())
        println("success job: ${execution.status}")
    } catch (e: Exception) {
        e.printStackTrace()
        println("failed job")
    }
}
