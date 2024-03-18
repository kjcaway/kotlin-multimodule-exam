package me.multimoduleexam.modulebatch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class JobConfig {

    @Bean
    fun simpleJob1(jobRepository: JobRepository, simpleStep1: Step): Job {
        return JobBuilder("simpleJob", jobRepository)
            .start(simpleStep1)
            .build()
    }

    @Bean
    fun simpleStep1(
        jobRepository: JobRepository,
        testTasklet: Tasklet,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("simpleStep1", jobRepository)
            .tasklet(testTasklet, platformTransactionManager).build()
    }

    @Bean
    fun testTasklet(): Tasklet {
        return (Tasklet { contribution: StepContribution?, chunkContext: ChunkContext? ->
            println("tasklet")
            RepeatStatus.FINISHED
        })
    }
}