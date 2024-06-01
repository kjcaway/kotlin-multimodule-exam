package me.multimoduleexam.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.DateTimeException
import java.time.LocalDateTime

class ReservationTimeValidator : ConstraintValidator<ReservationTimeChecker, LocalDateTime> {
    override fun isValid(value: LocalDateTime?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return false

//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
//        val str = value.toString()
//        val date = LocalDateTime.parse(str, formatter)
        return try {
            return value.isAfter(LocalDateTime.now())
        } catch (e: DateTimeException) {
            false
        }
    }
}