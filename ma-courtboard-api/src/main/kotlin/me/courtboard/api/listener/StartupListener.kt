package me.courtboard.api.listener

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class StartupListener(
    @Value("\${spring.profiles.active}")
    private val activeProfile: String,
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        println("=============================")
        println("Application is ready. active profile: ${activeProfile}")
        println("=============================")
        return
    }
}