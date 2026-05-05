package me.courtboard.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig(
    @Value("\${app.allowed-origin}")
    val allowedOrigin: String,
    @Value("\${storage.path}")
    val storagePath: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigin)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val location = "file:" + Paths.get(storagePath).toAbsolutePath().normalize().toString() + "/"
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location)
            .setCachePeriod(60 * 60 * 24 * 7) // 1주일
    }
}