import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled
import org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.helpers.AnnotationHelper.findAnnotation
import java.io.IOException
import java.lang.String.format
import java.lang.reflect.AnnotatedElement
import java.net.InetSocketAddress
import java.net.Socket


class EnabledIfReachableCondition : ExecutionCondition {

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        val element = context.element.orElseThrow { IllegalStateException() }
        return findAnnotation(element, EnabledIfReachable::class.java)
            ?.let { disableIfUnreachable(it, element) }
            ?: enabled("@EnabledIfReachable is not present")
    }

    private fun disableIfUnreachable(
        annotation: EnabledIfReachable,
        element: AnnotatedElement
    ): ConditionEvaluationResult {
        val host = annotation.host
        val port = annotation.port
        val timeoutMillis = annotation.timeoutMillis

        return try {
            val reachable = pingHost(host, port, timeoutMillis)
            if (reachable)
                enabled(format("%s is enabled because %s is reachable", element, host))
            else
                disabled(
                    format(
                        "%s is disabled because %s could not be reached in %dms",
                        element, host, timeoutMillis
                    )
                )
        } catch (e: Exception) {
            // 연결 시도 중 예외 발생 시 테스트를 disable 처리
            disabled(
                format(
                    "%s is disabled because %s check failed with: %s",
                    element, host, e.message ?: "unknown error"
                )
            )
        }
    }

    private fun pingHost(host: String, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeout)
                return true
            }
        } catch (e: IOException) {
            return false
        }
    }
}