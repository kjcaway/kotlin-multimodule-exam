import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled
import org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.helpers.AnnotationHelper.findAnnotation
import java.lang.String.format
import java.lang.reflect.AnnotatedElement
import java.net.HttpURLConnection
import java.net.URL

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
        val url = annotation.url
        val timeoutMillis = annotation.timeoutMillis
        val reachable = pingUrl(url, timeoutMillis)

        return if (reachable)
            enabled(format("%s is enabled because %s is reachable", element, url))
        else
            disabled(
                format(
                    "%s is disabled because %s could not be reached in %dms",
                    element, url, timeoutMillis
                )
            )
    }

    private fun pingUrl(url: String, timeoutMillis: Int): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis
            connection.requestMethod = "HEAD"

            return true
        } catch (e: Exception) {
            false
        }
    }
}