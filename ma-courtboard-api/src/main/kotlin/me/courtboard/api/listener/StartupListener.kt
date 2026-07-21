package me.courtboard.api.listener

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class StartupListener(
    @Value("\${spring.profiles.active}")
    private val activeProfile: String,
) : ApplicationListener<ApplicationReadyEvent> {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        log.info("Application is ready. active profile: {}", activeProfile)
    }
}