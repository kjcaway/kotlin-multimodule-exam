package me.multimouleexam.moduleapi

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.ext.ScriptUtils
import org.testcontainers.jdbc.JdbcDatabaseDelegate


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
abstract class IntegrationTest {

    companion object {
        private var mySQLContainer: MySQLContainer<*>
        private const val IMAGE_NAME = "mysql:8.0.28"
        private const val PORT = 3306

        init {
            mySQLContainer = MySQLContainer(IMAGE_NAME)
                .withDatabaseName("testdb")
                .withUsername("root")
                .withPassword("root")
                .withInitScript("testcontainers/ddl.sql")
                .waitingFor(Wait.forHttp("/"))
                .withReuse(true)
        }

        @JvmStatic
        fun get(): MySQLContainer<*> {
            return mySQLContainer;
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            if (!mySQLContainer.isRunning) {
                mySQLContainer.start()
                val containerDelegate = JdbcDatabaseDelegate(mySQLContainer, "")
                ScriptUtils.runInitScript(
                    containerDelegate,
                    "testcontainers/dml.sql"
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            mySQLContainer.close()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl)
            registry.add("spring.datasource.host", mySQLContainer::getHost)
            registry.add("spring.datasource.port", mySQLContainer::getFirstMappedPort)
        }
    }
}