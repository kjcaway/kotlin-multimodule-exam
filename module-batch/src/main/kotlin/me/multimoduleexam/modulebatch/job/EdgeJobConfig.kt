package me.multimoduleexam.modulebatch.job

import me.multimoduleexam.modulebatch.service.EdgeBatchService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class EdgeJobConfig(
    private val edgeBatchService: EdgeBatchService
) {

    @Bean
    fun edgeBatchJob(jobRepository: JobRepository, edgeBatchJobStep: Step): Job {
        return JobBuilder("edgeBatchJob", jobRepository)
            .start(edgeBatchJobStep)
            .build()
    }

    @Bean
    fun edgeBatchJobStep(
        jobRepository: JobRepository,
        edgeBatchTasklet: Tasklet,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("edgeBatchJobStep", jobRepository)
            .tasklet(edgeBatchTasklet, platformTransactionManager).build()
    }

    @Bean
    fun edgeBatchTasklet(): Tasklet {
        return (Tasklet { contribution, chunkContext ->
            val edgeCount = chunkContext.stepContext.jobParameters["edgeCount"] ?: 1000
            edgeBatchService.insertRandomEdge(edgeCount as Int)
            RepeatStatus.FINISHED
        })
    }
}