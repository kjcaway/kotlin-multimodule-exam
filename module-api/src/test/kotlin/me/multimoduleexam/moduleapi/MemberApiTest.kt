package me.multimoduleexam.moduleapi

import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import org.hamcrest.Matchers.*

@Tag("api")
@ExtendWith(MockitoExtension::class)
@ExtendWith(SpringExtension::class)
@TestMethodOrder(MethodOrderer.MethodName::class)
@AutoConfigureMockMvc
class MemberApiTest @Autowired constructor(
    val mockMvc: MockMvc
) : IntegrationTest() {

    @Test
    fun `_01 test`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers.content().contentType("text/plain;charset=UTF-8")
            )
    }
}