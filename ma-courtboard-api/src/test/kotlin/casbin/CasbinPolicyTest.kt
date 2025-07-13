package casbin

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CasbinPolicyTest() {
    private lateinit var modelPath: String
    private lateinit var policyPath: String
    private lateinit var enforcer: Enforcer

    private fun init() {
        modelPath = Paths.get("src/test/resources/casbin/model.conf").toAbsolutePath().toString()
        policyPath = Paths.get("src/test/resources/casbin/policy.csv").toAbsolutePath().toString()

        enforcer = Enforcer(modelPath, policyPath)
    }

    @Disabled
    @Test
    fun `test`() {
        init()

        enforcer.clearPolicy()
        enforcer.roleManager.clear()

        enforcer.addNamedPolicy("p", "admin", "courtboard", "*", "(get|post|put|delete)")
        enforcer.addNamedPolicy("p", "user", "courtboard", "/api/tactics", "(get)")

        enforcer.addNamedGroupingPolicy("g", "1", "admin", "courtboard")
        enforcer.addNamedGroupingPolicy("g", "2", "user", "courtboard")

        enforcer.isAutoNotifyDispatcher = true
        println("[[p]]")
        enforcer.policy.forEach { println(it) }
        println("[[g]]")
        enforcer.groupingPolicy.forEach { println(it) }
        println("[[role]]")
        enforcer.roleManager.printRoles()

        println("----------------")
        println("------TEST------")
        println("----------------")
        Assertions.assertTrue(enforcer.enforce("1", "courtboard", "/api/tactics", "get"))
        Assertions.assertTrue(enforcer.enforce("1", "courtboard", "/api/tactics", "post"))
        Assertions.assertTrue(enforcer.enforce("1", "courtboard", "/api/member", "get"))
        Assertions.assertTrue(enforcer.enforce("2", "courtboard", "/api/tactics", "get"))
        Assertions.assertFalse(enforcer.enforce("2", "courtboard", "/api/member", "get"))
        Assertions.assertFalse(enforcer.enforce("2", "courtboard", "/api/member", "get"))
    }

    @Test
    fun `test_courtboard`() {
        init()

        enforcer.addNamedGroupingPolicy("g", "1", "admin", "courtboard")
        enforcer.addNamedGroupingPolicy("g", "2", "user", "courtboard")

        enforcer.isAutoNotifyDispatcher = true
        println("[[p]]")
        enforcer.policy.forEach { println(it) }
        println("[[g]]")
        enforcer.groupingPolicy.forEach { println(it) }
        println("[[role]]")
        enforcer.roleManager.printRoles()

        println("----------------")
        println("------TEST------")
        println("----------------")
        Assertions.assertTrue(enforcer.enforce("1", "courtboard", "/api/tactics", "get"))
        Assertions.assertTrue(enforcer.enforce("1", "courtboard", "/api/tactics", "post"))
        Assertions.assertTrue(enforcer.enforce("1", "courtboard", "/api/member", "get"))
        Assertions.assertTrue(enforcer.enforce("2", "courtboard", "/api/tactics", "get"))
        Assertions.assertFalse(enforcer.enforce("2", "courtboard", "/api/member", "get"))
        Assertions.assertFalse(enforcer.enforce("2", "courtboard", "/api/member", "get"))
    }

    @Test
    fun `test_delete_user`() {
        init()

        enforcer.addNamedGroupingPolicy("g", "1", "admin", "courtboard")
        enforcer.addNamedGroupingPolicy("g", "2", "user", "courtboard")

        enforcer.isAutoNotifyDispatcher = true
        println("[[p]]")
        enforcer.policy.forEach { println(it) }
        println("[[g]]")
        enforcer.groupingPolicy.forEach { println(it) }
        println("[[role]]")
        enforcer.roleManager.printRoles()

        println("----------------")
        println("------TEST------")
        println("----------------")
        enforcer.deleteUser("1")
        Assertions.assertFalse(enforcer.enforce("1", "courtboard", "/api/my/account", "delete"))
    }
}