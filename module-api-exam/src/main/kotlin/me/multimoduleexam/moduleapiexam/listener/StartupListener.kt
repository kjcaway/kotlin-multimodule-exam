package me.multimoduleexam.moduleapiexam.listener

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class StartupListener(
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        println("start up with profile: ${System.getProperty("spring.profiles.active")}")
        return
    }
}