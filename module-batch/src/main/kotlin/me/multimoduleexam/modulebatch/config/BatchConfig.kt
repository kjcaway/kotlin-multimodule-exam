package me.multimoduleexam.modulebatch.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Order(-1)
@Configuration
class BatchConfig(
    @Value("\${spring.datasource.url:}") val dbUrl: String,
    @Value("\${spring.datasource.username:}") val dbUsername: String,
    @Value("\${spring.datasource.password:}") val dbPassword: String
) {
    @Primary
    @Bean
    fun dataSource(): DataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = "com.mysql.cj.jdbc.Driver"
        hikariConfig.jdbcUrl = dbUrl
        hikariConfig.username = dbUsername
        hikariConfig.password = dbPassword
        return HikariDataSource(hikariConfig)
    }

    @Bean(name = ["subDatasource"])
    fun subDatasource(): DataSource {
        val embeddedDatabaseBuilder = EmbeddedDatabaseBuilder()
        return embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.H2)
            .addScript("/org/springframework/batch/core/schema-h2.sql")
            .build()
    }

    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return JpaTransactionManager()
    }

    @Bean
    fun jobRepository(): JobRepository {
        val factory = JobRepositoryFactoryBean()
        factory.setDataSource(subDatasource())
        factory.transactionManager = transactionManager()
        factory.afterPropertiesSet()
        return factory.getObject()
    }

    @Bean
    fun jobLauncher(): JobLauncher {
        val jobLauncher = TaskExecutorJobLauncher()
        jobLauncher.setJobRepository(jobRepository())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}