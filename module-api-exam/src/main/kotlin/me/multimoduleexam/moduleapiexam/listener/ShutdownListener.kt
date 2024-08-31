package me.multimoduleexam.moduleapiexam.listener

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component

@Component
class ShutdownListener : ApplicationListener<ContextClosedEvent> {

    override fun onApplicationEvent(event: ContextClosedEvent) {
        println("shutdown application.")
        return
    }
}