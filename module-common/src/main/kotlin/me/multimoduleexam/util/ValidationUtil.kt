package me.multimoduleexam.util

object ValidationUtil {
    /**
     * check valid email format
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        return emailRegex.matches(email)
    }

    /**
     *  check valid mobile number format
     */
    fun isValidMobileNumber(number: String): Boolean {
        val mobileNumberRegex = Regex("^\\+?[0-9]{6,14}\$")
        return mobileNumberRegex.matches(number)
    }
}