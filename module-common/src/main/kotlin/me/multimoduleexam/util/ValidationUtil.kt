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

    fun isXssSafe(input: String): Boolean {
        // HTML 태그, 스크립트, 위험한 문자열 패턴 정의
        val xssPatterns = listOf(
            "<script[^>]*>.*?</script>",           // 스크립트 태그
            "<.*?javascript:.*?>",                 // javascript: 프로토콜
            "<.*?\\son\\w+=(\'|\").*?(\'|\").*?>", // 인라인 이벤트
            "eval\\((.*?)\\)",                     // eval() 함수
            "expression\\((.*?)\\)",               // expression() 함수
            "javascript:",                         // javascript: 프로토콜
            "vbscript:",                          // vbscript: 프로토콜
            "onload(.*?)=",                       // onload 이벤트
            "<.*?\\s+on\\w+=[^>]*>",              // 기타 on* 이벤트
            ".*?data:.*?,.*?"                     // data: URI scheme
        )

        // 정규식 패턴을 하나의 정규식으로 결합
        val combinedPattern = xssPatterns.joinToString("|").toRegex(RegexOption.IGNORE_CASE)

        // 입력값이 패턴과 매치되지 않으면 안전한 것으로 판단
        return !combinedPattern.containsMatchIn(input)
    }
}