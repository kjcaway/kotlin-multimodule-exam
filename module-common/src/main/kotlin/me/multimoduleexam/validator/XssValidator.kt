package me.multimoduleexam.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import me.multimoduleexam.util.ValidationUtil

class XssValidator : ConstraintValidator<XssChecker, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return false

        return try {
            return ValidationUtil.isXssSafe(value)
        } catch (e: Exception) {
            false
        }
    }
}