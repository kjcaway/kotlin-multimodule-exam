
import org.junit.jupiter.api.extension.ExtendWith


@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(EnabledIfReachableCondition::class)
annotation class EnabledIfReachable(
    val host: String,
    val port: Int,
    val timeoutMillis: Int
)