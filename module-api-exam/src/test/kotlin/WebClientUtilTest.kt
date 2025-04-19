import me.multimoduleexam.moduleapiexam.util.WebClientUtil
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class WebClientUtilTest {

    @EnabledIfReachable(
        url = "http://localhost:8081",
        timeoutMillis = 1000
    )
    @Test
    fun `test web client`() {
        val res = WebClientUtil
            .get("http://localhost:8081/api/hello", mapOf("Content-Type" to "application/json"))
            .block()

        println(res)
    }
}