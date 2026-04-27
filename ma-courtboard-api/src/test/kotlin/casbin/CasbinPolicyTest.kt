package casbin

import me.courtboard.api.global.Constants
import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Paths

class CasbinPolicyTest {
    private val modelPath = Paths.get("src/test/resources/casbin/model.conf").toAbsolutePath().toString()
    private val policyPath = Paths.get("src/test/resources/casbin/policy.csv").toAbsolutePath().toString()
    private lateinit var enforcer: Enforcer

    @BeforeEach
    fun setUp() {
        enforcer = Enforcer(modelPath, policyPath).apply {
            isAutoNotifyDispatcher = true
            addNamedGroupingPolicy("g", ADMIN_USER_ID, Constants.ROLE_ADMIN, Constants.COURTBOARD)
            addNamedGroupingPolicy("g", USER_USER_ID, Constants.ROLE_USER, Constants.COURTBOARD)
        }
    }

    @ParameterizedTest(name = "user={0} {3} {2} -> {4}")
    @CsvSource(
        "1, courtboard, /api/tactics, get,    true",
        "1, courtboard, /api/tactics, post,   true",
        "1, courtboard, /api/member,  get,    true",
        "2, courtboard, /api/tactics, get,    true",
        "2, courtboard, /api/member,  get,    false",
        "2, courtboard, /api/member,  post,   false",
    )
    fun `enforces policy by role and domain`(
        sub: String,
        dom: String,
        obj: String,
        act: String,
        expected: Boolean,
    ) {
        assertEquals(expected, enforcer.enforce(sub, dom, obj, act))
    }

    @Test
    fun `deleteUser revokes all permissions in domain`() {
        enforcer.deleteUser(ADMIN_USER_ID)

        assertFalse(enforcer.enforce(ADMIN_USER_ID, Constants.COURTBOARD, "/api/my/account", "delete"))
    }

    companion object {
        private const val ADMIN_USER_ID = "1"
        private const val USER_USER_ID = "2"
    }
}
