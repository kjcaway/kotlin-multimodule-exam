package me.multimoduleexam.validator

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [XssValidator::class])
annotation class XssChecker(
    val message: String = "default message",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)