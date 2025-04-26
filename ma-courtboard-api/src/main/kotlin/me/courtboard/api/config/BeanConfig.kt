package me.courtboard.api.config

import me.multimoduleexam.cache.LocalStorage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {
    @Bean
    fun localStorage(): LocalStorage<String, String> {
        return LocalStorage(300) // 5분 만료
    }
}